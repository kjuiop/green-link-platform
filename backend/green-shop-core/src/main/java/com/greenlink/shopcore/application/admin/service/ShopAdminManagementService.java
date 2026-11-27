package com.greenlink.shopcore.application.admin.service;

import com.greenlink.shopcore.application.access.port.CompanyAccessGranter;
import com.greenlink.shopcore.application.admin.dto.CreateMemberCommand;
import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.admin.usecase.ShopAdminManagementUseCase;
import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.dto.ManagedAccountCommand;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityRegistrar;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.application.common.ManagementGuard;
import com.greenlink.shopcore.domain.auth.ShopAccessPolicy;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.domain.brand.port.ShopBrandMemberRepository;
import com.greenlink.shopcore.domain.brand.port.ShopBrandRepository;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShopAdminManagementService implements ShopAdminManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(ShopAdminManagementService.class);

    private final ShopAdminMemberRepository memberRepository;
    private final ShopBrandRepository brandRepository;
    private final ShopBrandMemberRepository brandMemberRepository;
    private final IdentityVerifier identityVerifier;
    private final IdentityRegistrar identityRegistrar;
    private final CompanyAccessGranter accessGranter;
    private final ManagementGuard guard;
    private final MemberApprovalWriter approvalWriter;
    private final Clock clock;

    public ShopAdminManagementService(ShopAdminMemberRepository memberRepository,
                                      ShopBrandRepository brandRepository,
                                      ShopBrandMemberRepository brandMemberRepository,
                                      IdentityVerifier identityVerifier,
                                      IdentityRegistrar identityRegistrar,
                                      CompanyAccessGranter accessGranter,
                                      ManagementGuard guard,
                                      MemberApprovalWriter approvalWriter,
                                      Clock clock) {
        this.memberRepository = memberRepository;
        this.brandRepository = brandRepository;
        this.brandMemberRepository = brandMemberRepository;
        this.identityVerifier = identityVerifier;
        this.identityRegistrar = identityRegistrar;
        this.accessGranter = accessGranter;
        this.guard = guard;
        this.approvalWriter = approvalWriter;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<ShopAdminView> list(ManagementActor actor, ShopAdminStatus status, Long companyId) {
        Long verifiedCompanyId = guard.requireManager(actor);
        Long targetCompanyId = actor.role() == ShopRole.PARTNER_ADMIN ? verifiedCompanyId : companyId;
        List<ShopAdminMember> members = targetCompanyId != null
            ? memberRepository.findAllByCompanyId(targetCompanyId, status)
            : memberRepository.findAll(status);
        if (members.isEmpty()) {
            return List.of();
        }
        Map<Long, AccountProfile> profiles = loadProfiles(members);
        Map<Long, String> companyNames = loadCompanyNames(members, profiles);
        Map<Long, Set<Long>> brandIds = brandMemberRepository.findBrandIdsByMemberIds(
            members.stream().map(ShopAdminMember::getId).toList());
        return members.stream()
            .map(m -> ShopAdminView.of(m, profiles.get(m.getAdminId()), companyNames.get(m.getCompanyId()),
                brandIds.get(m.getId())))
            .toList();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void approve(ManagementActor actor, Long id, ShopRole grantedRole, Set<Long> brandIds) {
        ShopAdminMember member = findManageable(actor, id);
        ShopRole role = grantedRole != null ? grantedRole : member.getRequestedRole();
        member.validateApproval(role);
        if (role != ShopRole.PARTNER_ADMIN && !memberRepository.existsApprovedByCompanyId(member.getCompanyId())) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_FIRST_MEMBER_MUST_BE_PARTNER);
        }
        Set<Long> assignedBrandIds = role == ShopRole.BRAND_ADMIN
            ? requireBrands(member.getCompanyId(), brandIds, true)
            : Set.of();

        accessGranter.grant(member.getAdminId(), member.getCompanyId(), role);

        LocalDateTime now = LocalDateTime.now(clock);
        member.approve(role, actor.adminId(), now);
        approvalWriter.save(member, assignedBrandIds, now);
    }

    @Override
    public void reject(ManagementActor actor, Long id, String reason) {
        ShopAdminMember member = findManageable(actor, id);
        member.reject(reason, actor.adminId(), LocalDateTime.now(clock));
        memberRepository.save(member);
    }

    @Override
    public void changeRole(ManagementActor actor, Long id, ShopRole newRole) {
        ShopAdminMember member = findManageable(actor, id);
        ShopRole previousRole = member.getRole();
        member.changeRole(newRole);
        if (previousRole == ShopRole.PARTNER_ADMIN && newRole != ShopRole.PARTNER_ADMIN) {
            requireNotLastPartner(member);
        }
        accessGranter.changeRole(member.getAdminId(), newRole);
        memberRepository.save(member);
        if (newRole == ShopRole.PARTNER_ADMIN) {
            brandMemberRepository.unassignAllByMemberId(member.getId(), LocalDateTime.now(clock));
        }
    }

    @Override
    public void assignBrands(ManagementActor actor, Long id, Set<Long> brandIds) {
        ShopAdminMember member = findManageable(actor, id);
        if (member.getStatus() != ShopAdminStatus.APPROVED) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_NOT_APPROVED);
        }
        if (member.getRole() != ShopRole.BRAND_ADMIN) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_BRAND_INVALID);
        }
        Set<Long> valid = requireBrands(member.getCompanyId(), brandIds, false);
        brandMemberRepository.replaceAssignments(member.getId(), valid, LocalDateTime.now(clock));
    }

    @Override
    public void remove(ManagementActor actor, Long id) {
        ShopAdminMember member = findManageable(actor, id);
        if (member.isApprovedAs(ShopRole.PARTNER_ADMIN)) {
            requireNotLastPartner(member);
        }
        LocalDateTime now = LocalDateTime.now(clock);
        member.remove(actor.adminId(), now);
        memberRepository.save(member);
        brandMemberRepository.unassignAllByMemberId(member.getId(), now);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long create(ManagementActor actor, CreateMemberCommand cmd) {
        Long verifiedCompanyId = guard.requireManager(actor);
        Long companyId = actor.role() == ShopRole.PARTNER_ADMIN ? verifiedCompanyId : cmd.companyId();
        if (companyId == null) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_COMPANY_REQUIRED);
        }
        ShopRole role = cmd.role();
        if (role != ShopRole.PARTNER_ADMIN && role != ShopRole.BRAND_ADMIN) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        }
        if (role != ShopRole.PARTNER_ADMIN && !memberRepository.existsApprovedByCompanyId(companyId)) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_FIRST_MEMBER_MUST_BE_PARTNER);
        }
        Set<Long> assignedBrandIds = role == ShopRole.BRAND_ADMIN
            ? requireBrands(companyId, cmd.brandIds(), true)
            : Set.of();

        VerifiedIdentity account = identityRegistrar.createManagedAccount(
            new ManagedAccountCommand(cmd.loginId(), cmd.name(), cmd.phone(), cmd.email(), companyId, role));

        LocalDateTime now = LocalDateTime.now(clock);
        try {
            ShopAdminMember member = ShopAdminMember.createApproved(account.adminId(), companyId, role, actor.adminId(), now);
            return approvalWriter.saveNew(member, assignedBrandIds, now);
        } catch (RuntimeException e) {
            log.error("온우림 계정은 생성됐으나 shop 멤버 저장 실패 - 수동 복구 필요: adminId={}, companyId={}, role={}",
                account.adminId(), companyId, role, e);
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void reissuePasswordSetup(ManagementActor actor, Long id) {
        ShopAdminMember member = findManageable(actor, id);
        if (member.getStatus() != ShopAdminStatus.APPROVED) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_NOT_APPROVED);
        }
        identityRegistrar.reissuePasswordSetup(member.getAdminId());
    }

    private Set<Long> requireBrands(Long companyId, Set<Long> brandIds, boolean atLeastOne) {
        Set<Long> requested = brandIds == null ? Set.of() : brandIds;
        if (atLeastOne && requested.isEmpty()) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_BRAND_REQUIRED);
        }
        Set<Long> companyBrandIds = brandRepository.findAllByCompanyId(companyId).stream()
            .map(ShopBrand::getId)
            .collect(Collectors.toSet());
        if (!companyBrandIds.containsAll(requested)) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_BRAND_INVALID);
        }
        return requested;
    }

    private void requireNotLastPartner(ShopAdminMember member) {
        if (memberRepository.countApprovedByCompanyIdAndRole(member.getCompanyId(), ShopRole.PARTNER_ADMIN) <= 1) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_LAST_PARTNER);
        }
    }

    private Map<Long, AccountProfile> loadProfiles(List<ShopAdminMember> members) {
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

    private ShopAdminMember findManageable(ManagementActor actor, Long id) {
        Long verifiedCompanyId = guard.requireManager(actor);
        ShopAdminMember member = find(id);
        if (!ShopAccessPolicy.canManageCompany(actor.role(), verifiedCompanyId, member.getCompanyId())) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        }
        return member;
    }

    private ShopAdminMember find(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new ShopBusinessException(ShopErrorCode.ADMIN_REQUEST_NOT_FOUND));
    }
}
