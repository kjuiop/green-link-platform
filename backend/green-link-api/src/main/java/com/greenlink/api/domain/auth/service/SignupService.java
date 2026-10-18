package com.greenlink.api.domain.auth.service;

import com.greenlink.api.domain.auth.dto.SignupRequest;
import com.greenlink.api.domain.company.repository.CompanyRepository;
import com.greenlink.api.domain.user.repository.UserRepository;
import com.greenlink.core.domain.company.entity.Company;
import com.greenlink.core.domain.company.entity.CompanyType;
import com.greenlink.core.domain.user.entity.Role;
import com.greenlink.core.domain.user.entity.User;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SignupService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(CompanyRepository companyRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signup(SignupRequest req) {
        if (userRepository.existsByLoginId(req.loginId())) {
            throw new BusinessException(AuthErrorCode.DUPLICATE_LOGIN_ID);
        }

        List<Role> requestedRoles = req.roles().stream()
            .map(this::parseRole)
            .toList();

        CompanyType requestedType = resolveCompanyType(requestedRoles);

        Company company = companyRepository.findByBusinessNo(req.businessNo())
            .map(existing -> {
                existing.expandType(requestedType);
                return existing;
            })
            .orElseGet(() -> companyRepository.save(
                Company.create(
                    req.businessNo(), req.companyName(), req.ceoName(),
                    req.companyPhone(), req.address(), requestedType
                )
            ));

        User user = User.create(
            req.loginId(),
            passwordEncoder.encode(req.password()),
            req.name(),
            req.phone(),
            company
        );

        requestedRoles.forEach(user::addRole);

        userRepository.save(user);
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_ROLE);
        }
    }

    private CompanyType resolveCompanyType(List<Role> roles) {
        boolean hasOrdering = roles.contains(Role.ORDERING);
        boolean hasVendor = roles.contains(Role.VENDOR);
        if (hasOrdering && hasVendor) return CompanyType.BOTH;
        if (hasOrdering) return CompanyType.ORDERING;
        return CompanyType.VENDOR;
    }
}
