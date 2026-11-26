package com.greenlink.core.application.onurimapplication.service;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.usecase.ApplyOnurimUseCase;
import com.greenlink.core.application.onurimapplication.usecase.ManageOnurimApplicationUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyRegion;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplication;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import com.greenlink.core.domain.onurimapplication.port.OnurimApplicationRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import com.greenlink.core.global.exception.company.CompanyErrorCode;
import com.greenlink.core.global.exception.onurimapplication.OnurimApplicationErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OnurimApplicationService implements ApplyOnurimUseCase, ManageOnurimApplicationUseCase {

    private final OnurimApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final AdminRepository adminRepository;

    public OnurimApplicationService(OnurimApplicationRepository applicationRepository,
                                    CompanyRepository companyRepository,
                                    AdminRepository adminRepository) {
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public OnurimApplicationResult apply(Long adminId, Long companyId, CompanyType desiredType,
                                         OnurimApplicationInfo info) {
        Admin admin = adminRepository.findById(adminId)
            .filter(a -> !a.isDeleted())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND));
        if (!companyId.equals(admin.getCompanyId())) {
            throw new BusinessException(OnurimApplicationErrorCode.APPLICANT_MISMATCH);
        }
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));
        if (company.isOnurimEnabled()) {
            throw new BusinessException(OnurimApplicationErrorCode.ALREADY_ENABLED);
        }
        if (applicationRepository.existsPendingByCompanyId(companyId)) {
            throw new BusinessException(OnurimApplicationErrorCode.ALREADY_PENDING);
        }
        OnurimApplication saved = applicationRepository.save(
            OnurimApplication.request(companyId, adminId, desiredType, info));
        return toResult(saved, company, admin);
    }

    @Override
    public Optional<OnurimApplicationResult> getLatest(Long companyId) {
        return applicationRepository.findLatestByCompanyId(companyId).map(this::toResult);
    }

    @Override
    public Page<OnurimApplicationResult> list(OnurimApplicationStatus status, Pageable pageable) {
        Page<OnurimApplication> page = applicationRepository.findAllByStatus(status, pageable);
        List<OnurimApplication> apps = page.getContent();
        Map<Long, Company> companies = companyRepository
            .findAllByIdIn(apps.stream().map(OnurimApplication::getCompanyId).distinct().toList())
            .stream().collect(Collectors.toMap(Company::getId, Function.identity()));
        Map<Long, Admin> admins = adminRepository
            .findAllByIdIn(apps.stream().map(OnurimApplication::getApplicantAdminId).distinct().toList())
            .stream().collect(Collectors.toMap(Admin::getId, Function.identity()));
        return page.map(a -> toResult(a, companies.get(a.getCompanyId()), admins.get(a.getApplicantAdminId())));
    }

    @Override
    public OnurimApplicationResult get(Long id) {
        return toResult(find(id));
    }

    @Override
    @Transactional
    public OnurimApplicationResult approve(Long id, CompanyType type, Long operatorId) {
        OnurimApplication application = find(id);
        Company company = companyRepository.findById(application.getCompanyId())
            .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));
        Admin applicant = adminRepository.findById(application.getApplicantAdminId())
            .filter(a -> !a.isDeleted())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND));

        application.approve(type, operatorId, LocalDateTime.now());
        if (company.isOnurimEnabled()) {
            throw new BusinessException(OnurimApplicationErrorCode.ALREADY_ENABLED);
        }

        OnurimApplicationInfo info = application.getInfo();
        company.updatePhone(info.companyPhone());
        company.updateInfo(company.getCompanyName(), company.getBusinessNo(),
            info.faxNumber() != null ? info.faxNumber() : company.getFaxNumber(),
            info.address(), info.detailAddress());
        company.changeTaxInvoiceEmail(info.taxInvoiceEmail());
        company.enableOnurim(type);
        companyRepository.save(company);

        if (type == CompanyType.VENDOR || type == CompanyType.BOTH) {
            company.updateBankAccount(info.bankName(), info.accountNumber(), info.accountHolder());
            companyRepository.save(company);
            companyRepository.saveRegions(company.getId(), info.regions().stream()
                .map(r -> CompanyRegion.of(company.getId(), r.sido(), r.sigungu()))
                .toList());
        }

        applicant.enableOnurim();
        adminRepository.save(applicant);
        return toResult(applicationRepository.save(application), company, applicant);
    }

    @Override
    @Transactional
    public OnurimApplicationResult reject(Long id, String reason, Long operatorId) {
        OnurimApplication application = find(id);
        application.reject(reason, operatorId, LocalDateTime.now());
        return toResult(applicationRepository.save(application));
    }

    private OnurimApplication find(Long id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new BusinessException(OnurimApplicationErrorCode.NOT_FOUND));
    }

    private OnurimApplicationResult toResult(OnurimApplication a) {
        Company company = companyRepository.findById(a.getCompanyId()).orElse(null);
        Admin admin = adminRepository.findById(a.getApplicantAdminId()).orElse(null);
        return toResult(a, company, admin);
    }

    private OnurimApplicationResult toResult(OnurimApplication a, Company company, Admin admin) {
        OnurimApplicationInfo i = a.getInfo();
        return new OnurimApplicationResult(
            a.getId(), a.getCompanyId(), company != null ? company.getCompanyName() : null,
            a.getApplicantAdminId(), admin != null ? admin.getName() : null,
            a.getDesiredType(), a.getStatus(),
            i.companyPhone(), i.address(), i.detailAddress(), i.taxInvoiceEmail(), i.memo(),
            i.faxNumber(), i.bankName(), i.accountNumber(), i.accountHolder(), i.regions(),
            a.getApprovedType(), a.getRejectReason(), a.getProcessedAt(), a.getCreatedAt());
    }
}
