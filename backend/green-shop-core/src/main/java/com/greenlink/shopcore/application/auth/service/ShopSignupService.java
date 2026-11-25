package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.dto.RegisteredAccount;
import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import com.greenlink.shopcore.application.auth.dto.SignupResult;
import com.greenlink.shopcore.application.auth.port.IdentityRegistrar;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter.Bucket;
import com.greenlink.shopcore.application.auth.usecase.ShopSignupUseCase;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ShopSignupService implements ShopSignupUseCase {

    private static final Logger log = LoggerFactory.getLogger(ShopSignupService.class);

    private final IdentityRegistrar registrar;
    private final ShopAdminMemberRepository memberRepository;
    private final SignupRateLimiter rateLimiter;
    private final Clock clock;

    public ShopSignupService(IdentityRegistrar registrar,
                             ShopAdminMemberRepository memberRepository,
                             SignupRateLimiter rateLimiter,
                             Clock clock) {
        this.registrar = registrar;
        this.memberRepository = memberRepository;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    @Override
    public SignupResult signup(SignupCommand command, String clientKey) {
        acquire(Bucket.SIGNUP, clientKey);
        ShopRole requestedRole = parseRequestedRole(command.requestedRole());

        RegisteredAccount registered = registrar.register(command);
        Long adminId = registered.identity().adminId();
        LocalDateTime now = LocalDateTime.now(clock);
        try {
            ShopAdminMember member = registered.founder()
                ? ShopAdminMember.founder(adminId, registered.targetCompanyId(), now)
                : ShopAdminMember.request(adminId, registered.targetCompanyId(), requestedRole, now);
            memberRepository.save(member);
        } catch (RuntimeException e) {
            log.error("온우림 계정은 생성됐으나 shop 멤버 저장 실패 - 수동 복구 필요: adminId={}, targetCompanyId={}, founder={}",
                adminId, registered.targetCompanyId(), registered.founder(), e);
            throw e;
        }
        return new SignupResult(adminId, registered.founder());
    }

    @Override
    public boolean businessNoExists(String businessNo, String clientKey) {
        acquire(Bucket.LOOKUP, clientKey);
        return registrar.businessNoExists(businessNo);
    }

    private void acquire(Bucket bucket, String clientKey) {
        if (!rateLimiter.tryAcquire(bucket, clientKey)) {
            throw new ShopBusinessException(ShopErrorCode.SIGNUP_RATE_LIMITED);
        }
    }

    private ShopRole parseRequestedRole(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        ShopRole role;
        try {
            role = ShopRole.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new ShopBusinessException(ShopErrorCode.SIGNUP_INVALID_INPUT);
        }
        if (role == ShopRole.SUPER_ADMIN) {
            throw new ShopBusinessException(ShopErrorCode.SIGNUP_INVALID_INPUT);
        }
        return role;
    }
}
