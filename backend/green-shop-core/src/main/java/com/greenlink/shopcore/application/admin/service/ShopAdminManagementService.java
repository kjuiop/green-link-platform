package com.greenlink.shopcore.application.admin.service;

import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.access.port.CompanyAccessGranter;
import com.greenlink.shopcore.application.admin.usecase.ShopAdminManagementUseCase;
import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShopAdminManagementService implements ShopAdminManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(ShopAdminManagementService.class);

    private final ShopAdminMemberRepository memberRepository;
    private final IdentityVerifier identityVerifier;
    private final CompanyAccessGranter accessGranter;
    private final Clock clock;

    public ShopAdminManagementService(ShopAdminMemberRepository memberRepository,
                                      IdentityVerifier identityVerifier,
                                      CompanyAccessGranter accessGranter,
                                      Clock clock) {
        this.memberRepository = memberRepository;
        this.identityVerifier = identityVerifier;
        this.accessGranter = accessGranter;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<ShopAdminView> list(ShopAdminStatus status) {
        List<ShopAdminMember> members = memberRepository.findAll(status);
        Map<Long, AccountProfile> profiles = loadProfiles(members);
        Map<Long, String> companyNames = loadCompanyNames(members, profiles);
        return members.stream()
            .map(m -> ShopAdminView.of(m, profiles.get(m.getAdminId()), companyNames.get(m.getCompanyId())))
            .toList();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void approve(Long id, ShopRole grantedRole, Long actorAdminId) {
        ShopAdminMember member = find(id);
        ShopRole role = grantedRole != null ? grantedRole : member.getRequestedRole();
        member.validateApproval(role);
        if (role != ShopRole.PARTNER_ADMIN && !memberRepository.existsApprovedByCompanyId(member.getCompanyId())) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_FIRST_MEMBER_MUST_BE_PARTNER);
        }

        accessGranter.grant(member.getAdminId(), member.getCompanyId());

        member.approve(role, actorAdminId, LocalDateTime.now(clock));
        memberRepository.save(member);
    }

    @Override
    public void reject(Long id, String reason, Long actorAdminId) {
        ShopAdminMember member = find(id);
        member.reject(reason, actorAdminId, LocalDateTime.now(clock));
        memberRepository.save(member);
    }

    private Map<Long, AccountProfile> loadProfiles(List<ShopAdminMember> members) {
        if (members.isEmpty()) {
            return Map.of();
        }
        List<Long> adminIds = members.stream().map(ShopAdminMember::getAdminId).distinct().toList();
        try {
            return identityVerifier.getAccounts(adminIds).stream()
                .collect(Collectors.toMap(AccountProfile::adminId, Function.identity(), (a, b) -> a));
        } catch (RuntimeException e) {
            log.warn("관리자 이름 조회 실패, 이름 없이 목록을 반환합니다: {}", e.getMessage());
            return Map.of();
        }
    }

    private Map<Long, String> loadCompanyNames(List<ShopAdminMember> members, Map<Long, AccountProfile> profiles) {
        List<Long> companyIds = members.stream()
            .filter(m -> {
                AccountProfile p = profiles.get(m.getAdminId());
                return p == null || p.companyName() == null;
            })
            .map(ShopAdminMember::getCompanyId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (companyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return identityVerifier.getCompanyNames(companyIds);
        } catch (RuntimeException e) {
            log.warn("회사명 조회 실패, 회사명 없이 목록을 반환합니다: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private ShopAdminMember find(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new ShopBusinessException(ShopErrorCode.ADMIN_REQUEST_NOT_FOUND));
    }
}
