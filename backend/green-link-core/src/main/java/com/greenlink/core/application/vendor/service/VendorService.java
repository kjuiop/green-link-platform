package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import com.greenlink.core.application.vendor.dto.UpdateVendorCommand;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.usecase.CreateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorsUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorStatusUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.member.model.Member;
import com.greenlink.core.domain.member.model.Role;
import com.greenlink.core.domain.member.port.MemberRepository;
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
        CreateVendorUseCase, UpdateVendorUseCase, UpdateVendorStatusUseCase {

    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public VendorService(MemberRepository memberRepository,
                         CompanyRepository companyRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<VendorResult> getVendors(String status) {
        List<Member> vendorMembers = memberRepository.findAllByRole(Role.VENDOR);

        List<Long> companyIds = vendorMembers.stream()
            .map(Member::getCompanyId)
            .toList();
        Map<Long, Company> companyMap = companyRepository.findAllByIdIn(companyIds).stream()
            .collect(Collectors.toMap(Company::getId, c -> c));

        List<VendorResult> results = vendorMembers.stream()
            .map(m -> toResult(m, companyMap.get(m.getCompanyId())))
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
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));

        Company company = companyRepository.findById(member.getCompanyId())
            .orElse(null);

        return toResult(member, company);
    }

    @Override
    @Transactional
    public VendorResult createVendor(CreateVendorCommand cmd) {
        if (memberRepository.existsByLoginId(cmd.loginId())) {
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
            CompanyType.VENDOR
        );
        Company savedCompany = companyRepository.save(company);

        Member member = Member.create(
            cmd.loginId(),
            passwordEncoder.encode(cmd.password()),
            cmd.managerName(),
            cmd.phone(),
            savedCompany.getId()
        );
        member.addRole(Role.VENDOR);
        Member savedMember = memberRepository.save(member);

        return toResult(savedMember, savedCompany);
    }

    @Override
    @Transactional
    public VendorResult updateVendor(Long id, UpdateVendorCommand cmd) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));

        member.updateInfo(cmd.managerName(), cmd.phone());
        Member savedMember = memberRepository.save(member);

        Company company = companyRepository.findById(member.getCompanyId())
            .orElse(null);

        if (company != null) {
            String businessNo = (cmd.businessNo() != null && !cmd.businessNo().isBlank())
                ? cmd.businessNo() : null;
            company.updateInfo(
                cmd.companyName(),
                businessNo,
                cmd.address() != null ? cmd.address() : ""
            );
            Company savedCompany = companyRepository.save(company);
            return toResult(savedMember, savedCompany);
        }

        return toResult(savedMember, null);
    }

    @Override
    @Transactional
    public VendorResult updateVendorStatus(Long id, String action) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));

        if ("activate".equalsIgnoreCase(action)) {
            member.activate();
        } else if ("suspend".equalsIgnoreCase(action)) {
            member.suspend();
        } else {
            throw new IllegalArgumentException("유효하지 않은 action입니다: " + action);
        }

        Member savedMember = memberRepository.save(member);

        Company company = companyRepository.findById(member.getCompanyId())
            .orElse(null);

        return toResult(savedMember, company);
    }

    private VendorResult toResult(Member member, Company company) {
        return new VendorResult(
            member.getId(),
            company != null ? company.getCompanyName() : null,
            company != null ? company.getBusinessNo() : null,
            member.getName(),
            member.getPhone(),
            company != null ? company.getAddress() : null,
            member.getLoginId(),
            member.getStatus().name(),
            null
        );
    }
}
