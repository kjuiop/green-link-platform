package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.VendorLoginCommand;
import com.greenlink.core.application.auth.dto.VendorLoginResult;
import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.application.auth.usecase.VendorLoginUseCase;
import com.greenlink.core.domain.member.model.Member;
import com.greenlink.core.domain.member.model.MemberStatus;
import com.greenlink.core.domain.member.port.MemberRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VendorLoginService implements VendorLoginUseCase {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public VendorLoginService(MemberRepository memberRepository,
                               PasswordEncoder passwordEncoder,
                               TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public VendorLoginResult login(VendorLoginCommand cmd) {
        Member member = memberRepository.findByLoginId(cmd.loginId())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(cmd.password(), member.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (member.getStatus() == MemberStatus.PENDING) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_PENDING);
        }
        if (member.getStatus() == MemberStatus.INACTIVE) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE);
        }

        String token = tokenProvider.generateToken(member.getId(), List.of("VENDOR"));
        return new VendorLoginResult(token, member.getCompanyId());
    }
}
