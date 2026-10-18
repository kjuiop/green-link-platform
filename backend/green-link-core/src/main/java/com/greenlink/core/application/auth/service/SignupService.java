package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.SignupCommand;
import com.greenlink.core.application.auth.usecase.SignupUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.member.model.Member;
import com.greenlink.core.domain.member.model.Role;
import com.greenlink.core.domain.member.port.MemberRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SignupService implements SignupUseCase {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(CompanyRepository companyRepository,
                         MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void signup(SignupCommand cmd) {
        if (memberRepository.existsByLoginId(cmd.loginId())) {
            throw new BusinessException(AuthErrorCode.DUPLICATE_LOGIN_ID);
        }

        List<Role> roles = cmd.roles().stream()
            .distinct()
            .map(this::parseRole)
            .toList();

        CompanyType companyType = resolveCompanyType(roles);

        Company company = companyRepository.findByBusinessNo(cmd.businessNo())
            .map(existing -> {
                existing.expandType(companyType);
                return companyRepository.save(existing);
            })
            .orElseGet(() -> companyRepository.save(
                Company.create(cmd.businessNo(), cmd.companyName(), cmd.ceoName(),
                    cmd.companyPhone(), cmd.address(), companyType)
            ));

        Member member = Member.create(
            cmd.loginId(),
            passwordEncoder.encode(cmd.password()),
            cmd.name(),
            cmd.phone(),
            company.getId()
        );
        roles.forEach(member::addRole);

        memberRepository.save(member);
    }

    private Role parseRole(String role) {
        try {
            Role r = Role.valueOf(role);
            if (r == Role.SUPER_ADMIN) {
                throw new BusinessException(AuthErrorCode.INVALID_ROLE);
            }
            return r;
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_ROLE);
        }
    }

    private CompanyType resolveCompanyType(List<Role> roles) {
        boolean hasOrdering = roles.contains(Role.ORDERING);
        boolean hasVendor = roles.contains(Role.VENDOR);
        if (hasOrdering && hasVendor) return CompanyType.BOTH;
        if (hasOrdering) return CompanyType.ORDERING;
        if (hasVendor) return CompanyType.VENDOR;
        throw new BusinessException(AuthErrorCode.INVALID_ROLE);
    }
}
