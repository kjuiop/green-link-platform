package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import com.greenlink.core.application.vendor.dto.UpdateVendorCommand;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.application.vendor.usecase.CreateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorsUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorStatusUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.VendorSignupUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VendorService implements GetVendorsUseCase, GetVendorUseCase,
        CreateVendorUseCase, UpdateVendorUseCase, UpdateVendorStatusUseCase, VendorSignupUseCase {

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public VendorService(AdminRepository adminRepository,
                         CompanyRepository companyRepository,
                         PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<VendorResult> getVendors(String status) {
        List<Admin> vendorAdmins = adminRepository.findAllVendors();

        List<Long> companyIds = vendorAdmins.stream()
            .map(Admin::getCompanyId)
            .toList();
        Map<Long, Company> companyMap = companyRepository.findAllByIdIn(companyIds).stream()
            .collect(Collectors.toMap(Company::getId, c -> c));

        List<VendorResult> results = vendorAdmins.stream()
            .map(a -> toResult(a, companyMap.get(a.getCompanyId())))
            .toList();

        if (status == null || status.isBlank()) {
            return results;
        }
        return results.stream()
            .filter(r -> r.status().equalsIgnoreCase(status))
            .toList();
    }

    @Override
    public VendorResult getVendor(Long id) {
        Admin admin = adminRepository.findById(id)
            .filter(Admin::isVendor)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));

        Company company = companyRepository.findById(admin.getCompanyId())
            .orElse(null);

        return toResult(admin, company);
    }

    @Override
    @Transactional
    public VendorResult createVendor(CreateVendorCommand cmd) {
        if (adminRepository.existsByLoginId(cmd.loginId())) {
            throw new BusinessException(VendorErrorCode.DUPLICATE_LOGIN_ID);
        }

        if (cmd.businessNo() != null && !cmd.businessNo().isBlank()) {
            if (companyRepository.existsByBusinessNo(cmd.businessNo())) {
                throw new BusinessException(VendorErrorCode.DUPLICATE_BUSINESS_NO);
            }
        }

        String businessNo = (cmd.businessNo() != null && !cmd.businessNo().isBlank())
            ? cmd.businessNo() : null;

        Company company = Company.create(
            businessNo,
            cmd.companyName(),
            "",
            cmd.phone(),
            cmd.address() != null ? cmd.address() : "",
            cmd.detailAddress() != null ? cmd.detailAddress() : "",
            CompanyType.VENDOR
        );
        Company savedCompany = companyRepository.save(company);

        Admin vendorAdmin = Admin.createVendor(
            cmd.loginId(),
            passwordEncoder.encode(cmd.password()),
            cmd.managerName(),
            cmd.phone(),
            savedCompany.getId()
        );
        Admin savedAdmin = adminRepository.save(vendorAdmin);

        return toResult(savedAdmin, savedCompany);
    }

    @Override
    @Transactional
    public VendorResult updateVendor(Long id, UpdateVendorCommand cmd) {
        Admin admin = adminRepository.findById(id)
            .filter(Admin::isVendor)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));

        admin.updateProfile(cmd.managerName(), cmd.phone());
        Admin savedAdmin = adminRepository.save(admin);

        Company company = companyRepository.findById(admin.getCompanyId())
            .orElse(null);

        if (company != null) {
            String businessNo = (cmd.businessNo() != null && !cmd.businessNo().isBlank())
                ? cmd.businessNo() : null;
            company.updateInfo(
                cmd.companyName(),
                businessNo,
                cmd.address() != null ? cmd.address() : "",
                cmd.detailAddress() != null ? cmd.detailAddress() : ""
            );
            Company savedCompany = companyRepository.save(company);
            return toResult(savedAdmin, savedCompany);
        }

        return toResult(savedAdmin, null);
    }

    @Override
    @Transactional
    public VendorResult updateVendorStatus(Long id, String action) {
        Admin admin = adminRepository.findById(id)
            .filter(Admin::isVendor)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));

        if ("activate".equalsIgnoreCase(action)) {
            admin.activate();
        } else if ("suspend".equalsIgnoreCase(action)) {
            admin.suspend();
        } else {
            throw new IllegalArgumentException("유효하지 않은 action입니다: " + action);
        }

        Admin savedAdmin = adminRepository.save(admin);

        Company company = companyRepository.findById(admin.getCompanyId())
            .orElse(null);

        return toResult(savedAdmin, company);
    }

    @Override
    @Transactional
    public VendorResult signup(VendorSignupCommand cmd) {
        if (adminRepository.existsByLoginId(cmd.loginId())) {
            throw new BusinessException(VendorErrorCode.DUPLICATE_LOGIN_ID);
        }

        String businessNo = (cmd.businessNo() != null && !cmd.businessNo().isBlank())
            ? cmd.businessNo() : null;

        if (businessNo != null && companyRepository.existsByBusinessNo(businessNo)) {
            throw new BusinessException(VendorErrorCode.DUPLICATE_BUSINESS_NO);
        }

        Company company = Company.create(
            businessNo,
            cmd.companyName(),
            cmd.ceoName() != null ? cmd.ceoName() : "",
            cmd.companyPhone() != null ? cmd.companyPhone() : cmd.phone(),
            cmd.address() != null ? cmd.address() : "",
            cmd.detailAddress() != null ? cmd.detailAddress() : "",
            CompanyType.VENDOR
        );
        Company savedCompany = companyRepository.save(company);

        Admin vendorAdmin = Admin.createVendorRequest(
            cmd.loginId(),
            passwordEncoder.encode(cmd.password()),
            cmd.managerName(),
            cmd.phone(),
            savedCompany.getId()
        );
        Admin savedAdmin = adminRepository.save(vendorAdmin);

        return toResult(savedAdmin, savedCompany);
    }

    private VendorResult toResult(Admin admin, Company company) {
        return new VendorResult(
            admin.getId(),
            company != null ? company.getCompanyName() : null,
            company != null ? company.getBusinessNo() : null,
            admin.getName(),
            admin.getPhone(),
            company != null ? company.getAddress() : null,
            company != null ? company.getDetailAddress() : null,
            admin.getLoginId(),
            admin.getStatus().name(),
            null
        );
    }
}
