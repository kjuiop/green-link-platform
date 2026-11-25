package com.greenlink.shopcore.application.common;

import com.greenlink.shopcore.domain.auth.ShopAccessPolicy;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ManagementGuard {

    private final ShopAdminMemberRepository memberRepository;

    public ManagementGuard(ShopAdminMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /** 관리 권한(슈퍼관리자 또는 현재 승인된 파트너 관리자)을 확인하고, 파트너 관리자면 DB 기준 회사 ID를 반환한다. */
    public Long requireManager(ManagementActor actor) {
        if (actor.role() == ShopRole.SUPER_ADMIN) {
            return null;
        }
        if (actor.role() != ShopRole.PARTNER_ADMIN) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        }
        ShopAdminMember member = memberRepository.findByAdminId(actor.adminId())
            .filter(m -> m.isApprovedAs(ShopRole.PARTNER_ADMIN))
            .orElseThrow(() -> new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));
        return member.getCompanyId();
    }

    public void requireCanManage(ManagementActor actor, Long targetCompanyId) {
        Long verifiedCompanyId = requireManager(actor);
        if (!ShopAccessPolicy.canManageCompany(actor.role(), verifiedCompanyId, targetCompanyId)) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        }
    }
}
