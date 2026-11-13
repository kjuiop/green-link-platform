package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import com.greenlink.core.application.vendor.dto.UpdateVendorCommand;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.dto.VendorResult.RegionDto;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand.RegionInput;
import com.greenlink.core.domain.company.model.CompanyCapability;
import com.greenlink.core.domain.company.model.CompanyRegion;
import com.greenlink.core.application.vendor.usecase.ChangeVendorPasswordUseCase;
import org.springframework.transaction.annotation.Propagation;
import com.greenlink.core.application.vendor.usecase.CreateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.DeleteVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorsUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorStatusUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.UploadVendorDocumentUseCase;
import com.greenlink.core.application.vendor.usecase.VendorSignupUseCase;
import com.greenlink.core.domain.company.port.DocumentStorage;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VendorService implements GetVendorsUseCase, GetVendorUseCase,
        CreateVendorUseCase, UpdateVendorUseCase, UpdateVendorStatusUseCase,
        DeleteVendorUseCase, VendorSignupUseCase, UploadVendorDocumentUseCase,
        ChangeVendorPasswordUseCase {

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentStorage documentStorage;

    public VendorService(AdminRepository adminRepository,
                         CompanyRepository companyRepository,
                         PasswordEncoder passwordEncoder,
                         DocumentStorage documentStorage) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.documentStorage = documentStorage;
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
    public PageResult<VendorResult> getVendorsPaged(String status, Pageable pageable) {
        Page<Admin> adminPage = adminRepository.findVendorsPaged(status, pageable);
        List<Long> companyIds = adminPage.getContent().stream()
                .map(Admin::getCompanyId).filter(Objects::nonNull).toList();
        Map<Long, Company> companyMap = companyRepository.findAllByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, c -> c));
        Page<VendorResult> resultPage = adminPage.map(a -> toResult(a, companyMap.get(a.getCompanyId())));
        return PageResult.of(resultPage);
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
            cmd.companyPhone() != null && !cmd.companyPhone().isBlank() ? cmd.companyPhone() : cmd.phone(),
            (cmd.faxNumber() != null && !cmd.faxNumber().isBlank()) ? cmd.faxNumber() : null,
            cmd.address() != null ? cmd.address() : "",
            cmd.detailAddress() != null ? cmd.detailAddress() : "",
            parseCompanyType(cmd.companyType())
        );
        Company savedCompany = companyRepository.save(company);

        Admin vendorAdmin = Admin.createVendor(
            cmd.loginId(),
            passwordEncoder.encode(cmd.password()),
            cmd.managerName(),
            cmd.phone(),
            cmd.email(),
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

        admin.updateProfile(cmd.managerName(), cmd.phone(), cmd.email());
        Admin savedAdmin = adminRepository.save(admin);

        Company company = companyRepository.findById(admin.getCompanyId())
            .orElse(null);

        if (company != null) {
            String businessNo = (cmd.businessNo() != null && !cmd.businessNo().isBlank())
                ? cmd.businessNo() : null;
            company.updateInfo(
                cmd.companyName(),
                businessNo,
                (cmd.faxNumber() != null && !cmd.faxNumber().isBlank()) ? cmd.faxNumber() : null,
                cmd.address() != null ? cmd.address() : "",
                cmd.detailAddress() != null ? cmd.detailAddress() : ""
            );
            if (cmd.companyPhone() != null && !cmd.companyPhone().isBlank()) {
                company.updatePhone(cmd.companyPhone());
            }
            if (cmd.companyType() != null && !cmd.companyType().isBlank()) {
                company.updateType(parseCompanyType(cmd.companyType()));
            }
            company.updateBankAccount(cmd.bankName(), cmd.accountNumber(), cmd.accountHolder());
            Company savedCompany = companyRepository.save(company);

            if (cmd.regions() != null) {
                List<CompanyRegion> regions = cmd.regions().stream()
                    .map(r -> CompanyRegion.of(savedCompany.getId(), r.sido(), r.sigungu()))
                    .toList();
                companyRepository.saveRegions(savedCompany.getId(), regions);
            }
            if (cmd.capabilities() != null) {
                List<CompanyCapability> caps = cmd.capabilities().stream()
                    .map(CompanyCapability::valueOf)
                    .toList();
                companyRepository.saveCapabilities(savedCompany.getId(), caps);
            }

            return toResult(savedAdmin, savedCompany);
        }

        return toResult(savedAdmin, null);
    }

    @Override
    @Transactional
    public void deleteVendor(Long id) {
        Admin admin = adminRepository.findById(id)
            .filter(Admin::isVendor)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
        if (admin.isDeleted()) {
            throw new BusinessException(VendorErrorCode.VENDOR_ALREADY_DELETED);
        }
        String nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        admin.softDelete(nowKst);
        adminRepository.save(admin);
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

        CompanyType requestedType = resolveCompanyType(cmd.roles());

        Company savedCompany;
        if (businessNo != null && companyRepository.existsByBusinessNo(businessNo)) {
            Company existing = companyRepository.findByBusinessNo(businessNo)
                .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
            CompanyType prevType = existing.getType();
            existing.expandType(requestedType);
            savedCompany = existing.getType() != prevType ? companyRepository.save(existing) : existing;
        } else {
            Company company = Company.create(
                businessNo,
                cmd.companyName(),
                cmd.ceoName() != null ? cmd.ceoName() : "",
                cmd.companyPhone() != null ? cmd.companyPhone() : cmd.phone(),
                null,
                cmd.address() != null ? cmd.address() : "",
                cmd.detailAddress() != null ? cmd.detailAddress() : "",
                requestedType
            );
            savedCompany = companyRepository.save(company);
        }

        if (cmd.regions() != null && !cmd.regions().isEmpty()) {
            List<CompanyRegion> regions = cmd.regions().stream()
                .map(r -> CompanyRegion.of(savedCompany.getId(), r.sido(), r.sigungu()))
                .toList();
            companyRepository.saveRegions(savedCompany.getId(), regions);
        }

        if (cmd.capabilities() != null && !cmd.capabilities().isEmpty()) {
            List<CompanyCapability> caps = cmd.capabilities().stream()
                .map(CompanyCapability::valueOf)
                .toList();
            companyRepository.saveCapabilities(savedCompany.getId(), caps);
        }

        Admin vendorAdmin = Admin.createVendorRequest(
            cmd.loginId(),
            passwordEncoder.encode(cmd.password()),
            cmd.managerName(),
            cmd.phone(),
            cmd.email(),
            savedCompany.getId()
        );
        Admin savedAdmin = adminRepository.save(vendorAdmin);

        return toResult(savedAdmin, savedCompany);
    }

    private CompanyType resolveCompanyType(List<String> roles) {
        if (roles == null || roles.isEmpty()) return CompanyType.VENDOR;
        boolean hasOrdering = roles.contains("ORDERING");
        boolean hasVendor = roles.contains("VENDOR");
        if (hasOrdering && hasVendor) return CompanyType.BOTH;
        if (hasOrdering) return CompanyType.ORDERING;
        return CompanyType.VENDOR;
    }

    private CompanyType parseCompanyType(String companyType) {
        if (companyType == null || companyType.isBlank()) return CompanyType.VENDOR;
        try {
            return CompanyType.valueOf(companyType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CompanyType.VENDOR;
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public UploadVendorDocumentUseCase.Result uploadDocument(Long vendorId, String docType,
            String fileName, byte[] content, String contentType) {
        Admin admin = adminRepository.findById(vendorId)
            .filter(Admin::isVendor)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
        Company company = companyRepository.findById(admin.getCompanyId())
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));

        // S3 업로드: 외부 API 호출을 트랜잭션 밖에서 수행
        DocumentStorage.UploadResult uploaded = documentStorage.store(
                company.getId(), docType, fileName, content, contentType);

        // DB 저장: companyRepository.save()의 자체 @Transactional로 처리
        company.updateDocumentUrl(docType, uploaded.url());
        companyRepository.save(company);

        return new UploadVendorDocumentUseCase.Result(company.getBusinessRegUrl(), company.getBankBookUrl());
    }

    @Override
    @Transactional
    public void changePassword(Long vendorId, String newPassword) {
        Admin admin = adminRepository.findById(vendorId)
            .filter(Admin::isVendor)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
        admin.changePassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }

    private VendorResult toResult(Admin admin, Company company) {
        List<RegionDto> regions = List.of();
        List<String> capabilities = List.of();
        if (company != null) {
            regions = companyRepository.findRegionsByCompanyId(company.getId()).stream()
                .map(r -> new RegionDto(r.sido(), r.sigungu()))
                .toList();
            capabilities = companyRepository.findCapabilitiesByCompanyId(company.getId()).stream()
                .map(Enum::name)
                .toList();
        }
        return new VendorResult(
            admin.getId(),
            company != null ? company.getCompanyName() : null,
            company != null ? company.getBusinessNo() : null,
            company != null ? company.getFaxNumber() : null,
            company != null ? company.getPhone() : null,
            admin.getName(),
            admin.getPhone(),
            admin.getEmail(),
            company != null ? company.getAddress() : null,
            company != null ? company.getDetailAddress() : null,
            admin.getLoginId(),
            admin.getStatus().name(),
            admin.getCreatedAt(),
            company != null && company.getType() != null ? company.getType().name() : null,
            regions,
            capabilities,
            company != null ? company.getBusinessRegUrl() : null,
            company != null ? company.getBankBookUrl() : null,
            company != null ? company.getBankName() : null,
            company != null ? company.getAccountNumber() : null,
            company != null ? company.getAccountHolder() : null
        );
    }
}
