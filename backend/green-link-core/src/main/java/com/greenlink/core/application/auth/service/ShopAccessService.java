package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.CompanySummary;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.ShopAccessUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShopAccessService implements ShopAccessUseCase {

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;

    public ShopAccessService(AdminRepository adminRepository, CompanyRepository companyRepository) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public VerifiedAccount grant(Long adminId, Long companyId) {
        Admin admin = adminRepository.findById(adminId)
            .filter(a -> !a.isDeleted())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND));
        if (admin.getStatus() == AdminStatus.PENDING) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_PENDING);
        }
        if (admin.getStatus() == AdminStatus.INACTIVE) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE);
        }
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new BusinessException(AuthErrorCode.SHOP_ACCESS_COMPANY_NOT_FOUND));
        if (admin.getCompanyId() != null && !admin.getCompanyId().equals(companyId)) {
            throw new BusinessException(AuthErrorCode.SHOP_ACCESS_COMPANY_MISMATCH);
        }

        if (!company.isSiteEnabled()) {
            company.enableSite();
            company = companyRepository.save(company);
        }
        if (admin.getCompanyId() == null) {
            admin.attachToCompany(companyId);
            admin = adminRepository.save(admin);
        }
        return new VerifiedAccount(
            admin.getId(), admin.getLoginId(), admin.getName(),
            company.getId(), company.getCompanyName(),
            company.isOnurimEnabled(), company.isSiteEnabled(),
            admin.isOnurimEnabled(), admin.getRole().name());
    }

    @Override
    public List<CompanySummary> getCompanySummaries(Collection<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            return List.of();
        }
        return companyRepository.findAllByIdIn(List.copyOf(companyIds)).stream()
            .map(c -> new CompanySummary(c.getId(), c.getCompanyName()))
            .toList();
    }
}
