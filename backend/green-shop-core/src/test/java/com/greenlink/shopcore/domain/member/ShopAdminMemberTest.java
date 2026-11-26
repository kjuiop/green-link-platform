package com.greenlink.shopcore.domain.member;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.global.exception.ErrorCode;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopAdminMemberTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 23, 10, 0);
    private static final LocalDateTime T1 = T0.plusHours(1);

    private ShopAdminMember pending() {
        return ShopAdminMember.request(10L, 7L, ShopRole.PARTNER_ADMIN, T0);
    }

    private ShopAdminMember in(ShopAdminStatus status) {
        ShopRole role = status == ShopAdminStatus.APPROVED ? ShopRole.PARTNER_ADMIN : null;
        return ShopAdminMember.of(1L, 10L, 7L, role, ShopRole.PARTNER_ADMIN, status, T0, T1, 99L,
            status == ShopAdminStatus.REJECTED ? "사유" : null);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 창립자는_승인된_PARTNER_ADMIN으로_즉시_생성된다() {
        ShopAdminMember m = ShopAdminMember.founder(10L, 7L, T0);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(m.getCompanyId()).isEqualTo(7L);
        assertThat(m.getAdminId()).isEqualTo(10L);
        assertThat(m.getProcessedAt()).isEqualTo(T0);
    }

    @Test
    void 신청하면_PENDING으로_생성되고_역할은_아직_없다() {
        ShopAdminMember m = pending();

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getAdminId()).isEqualTo(10L);
        assertThat(m.getCompanyId()).isEqualTo(7L);
        assertThat(m.getRequestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(m.getRole()).isNull();
        assertThat(m.getRequestedAt()).isEqualTo(T0);
        assertThat(m.getProcessedAt()).isNull();
    }

    @Test
    void 슈퍼관리자_역할로는_신청할_수_없다() {
        assertCode(() -> ShopAdminMember.request(10L, 7L, ShopRole.SUPER_ADMIN, T0),
            ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        assertCode(() -> ShopAdminMember.request(10L, 7L, null, T0), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
    }

    @Test
    void 브랜드_관리자_역할로_신청할_수_있다() {
        ShopAdminMember m = ShopAdminMember.request(10L, 7L, ShopRole.BRAND_ADMIN, T0);

        assertThat(m.getRequestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
    }

    @Test
    void PENDING은_승인하면_요청한_역할이_부여된다() {
        ShopAdminMember m = ShopAdminMember.request(10L, 7L, ShopRole.BRAND_ADMIN, T0);

        m.approve(ShopRole.BRAND_ADMIN, 99L, T1);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(m.getProcessedAt()).isEqualTo(T1);
        assertThat(m.getProcessedBy()).isEqualTo(99L);
    }

    @Test
    void 신청보다_낮은_역할로_승인하면_낮은_역할이_부여된다() {
        ShopAdminMember m = pending();

        m.approve(ShopRole.BRAND_ADMIN, 99L, T1);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(m.getRequestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 신청보다_높은_역할로는_승인할_수_없고_상태가_바뀌지_않는다() {
        ShopAdminMember m = ShopAdminMember.request(10L, 7L, ShopRole.BRAND_ADMIN, T0);

        assertCode(() -> m.approve(ShopRole.PARTNER_ADMIN, 99L, T1), ShopErrorCode.ACCESS_ROLE_EXCEEDS_REQUEST);
        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getRole()).isNull();
    }

    @Test
    void 슈퍼관리자나_null_역할로는_승인할_수_없다() {
        assertCode(() -> pending().approve(ShopRole.SUPER_ADMIN, 99L, T1), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        assertCode(() -> pending().approve(null, 99L, T1), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
    }

    @Test
    void PENDING은_사유와_함께_거절할_수_있고_역할은_부여되지_않는다() {
        ShopAdminMember m = pending();

        m.reject("  서류 미비  ", 99L, T1);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REJECTED);
        assertThat(m.getRejectReason()).isEqualTo("서류 미비");
        assertThat(m.getProcessedBy()).isEqualTo(99L);
        assertThat(m.getRole()).isNull();
    }

    @Test
    void 거절_사유가_없거나_공백이면_거부된다() {
        assertCode(() -> pending().reject(null, 99L, T1), ShopErrorCode.ACCESS_REJECT_REASON_REQUIRED);
        assertCode(() -> pending().reject("   ", 99L, T1), ShopErrorCode.ACCESS_REJECT_REASON_REQUIRED);
    }

    @Test
    void PENDING이_아니면_승인_거절이_거부된다() {
        assertCode(() -> in(ShopAdminStatus.APPROVED).approve(ShopRole.PARTNER_ADMIN, 99L, T1), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        assertCode(() -> in(ShopAdminStatus.REJECTED).approve(ShopRole.PARTNER_ADMIN, 99L, T1), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        assertCode(() -> in(ShopAdminStatus.APPROVED).reject("사유", 99L, T1), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        assertCode(() -> in(ShopAdminStatus.REJECTED).reject("사유", 99L, T1), ShopErrorCode.ACCESS_INVALID_TRANSITION);
    }

    @Test
    void 거절된_계정은_재신청하면_PENDING이_되고_회사와_요청_역할이_갱신되며_처리_정보가_초기화된다() {
        ShopAdminMember m = in(ShopAdminStatus.REJECTED);

        m.reapply(8L, ShopRole.BRAND_ADMIN, T1.plusDays(1));

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getCompanyId()).isEqualTo(8L);
        assertThat(m.getRequestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(m.getRejectReason()).isNull();
        assertThat(m.getProcessedAt()).isNull();
        assertThat(m.getProcessedBy()).isNull();
        assertThat(m.getRequestedAt()).isEqualTo(T1.plusDays(1));
    }

    @Test
    void 대기중이거나_승인된_계정은_재신청이_거부된다() {
        assertCode(() -> in(ShopAdminStatus.PENDING).reapply(7L, ShopRole.PARTNER_ADMIN, T1),
            ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        assertCode(() -> in(ShopAdminStatus.APPROVED).reapply(7L, ShopRole.PARTNER_ADMIN, T1),
            ShopErrorCode.ACCESS_ALREADY_REQUESTED);
    }

    @Test
    void 재신청도_슈퍼관리자_역할은_거부되고_상태가_바뀌지_않는다() {
        ShopAdminMember m = in(ShopAdminStatus.REJECTED);

        assertCode(() -> m.reapply(7L, ShopRole.SUPER_ADMIN, T1), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REJECTED);
    }

    @Test
    void 승인된_계정은_역할을_변경할_수_있다() {
        ShopAdminMember m = in(ShopAdminStatus.APPROVED);

        m.changeRole(ShopRole.BRAND_ADMIN);

        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
    }

    @Test
    void 승인되지_않은_계정의_역할_변경은_거부된다() {
        assertCode(() -> pending().changeRole(ShopRole.BRAND_ADMIN), ShopErrorCode.MEMBER_NOT_APPROVED);
        assertCode(() -> in(ShopAdminStatus.REJECTED).changeRole(ShopRole.BRAND_ADMIN), ShopErrorCode.MEMBER_NOT_APPROVED);
        assertCode(() -> in(ShopAdminStatus.REMOVED).changeRole(ShopRole.BRAND_ADMIN), ShopErrorCode.MEMBER_NOT_APPROVED);
    }

    @Test
    void 역할_변경도_슈퍼관리자나_null_역할은_거부되고_기존_역할이_유지된다() {
        ShopAdminMember m = in(ShopAdminStatus.APPROVED);

        assertCode(() -> m.changeRole(ShopRole.SUPER_ADMIN), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        assertCode(() -> m.changeRole(null), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        assertThat(m.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 제거하면_REMOVED가_되고_처리_정보가_기록된다() {
        ShopAdminMember m = in(ShopAdminStatus.APPROVED);

        m.remove(55L, T1.plusDays(2));

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REMOVED);
        assertThat(m.getProcessedBy()).isEqualTo(55L);
        assertThat(m.getProcessedAt()).isEqualTo(T1.plusDays(2));
    }

    @Test
    void 이미_제거된_계정을_다시_제거하면_거부된다() {
        assertCode(() -> in(ShopAdminStatus.REMOVED).remove(55L, T1), ShopErrorCode.ACCESS_INVALID_TRANSITION);
    }

    @Test
    void 제거된_계정은_재신청하면_PENDING이_되고_기존_역할이_초기화된다() {
        ShopAdminMember m = in(ShopAdminStatus.APPROVED);
        m.remove(55L, T1);

        m.reapply(7L, ShopRole.BRAND_ADMIN, T1.plusDays(3));

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getRole()).isNull();
        assertThat(m.getRequestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(m.getProcessedAt()).isNull();
        assertThat(m.getProcessedBy()).isNull();
    }

    @Test
    void isApprovedAs는_승인_상태이면서_역할이_일치할_때만_참이다() {
        assertThat(in(ShopAdminStatus.APPROVED).isApprovedAs(ShopRole.PARTNER_ADMIN)).isTrue();
        assertThat(in(ShopAdminStatus.APPROVED).isApprovedAs(ShopRole.BRAND_ADMIN)).isFalse();
        assertThat(in(ShopAdminStatus.REMOVED).isApprovedAs(ShopRole.PARTNER_ADMIN)).isFalse();
        assertThat(pending().isApprovedAs(ShopRole.PARTNER_ADMIN)).isFalse();
    }

    @Test
    void 브랜드_관리자_신청은_희망_브랜드를_보관한다() {
        ShopAdminMember m = ShopAdminMember.request(10L, 7L, ShopRole.BRAND_ADMIN, Set.of(1L, 2L), T0);

        assertThat(m.getRequestedBrandIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(m.getRole()).isNull();
    }

    @Test
    void 브랜드_관리자가_아닌_신청은_희망_브랜드를_비운다() {
        assertThat(ShopAdminMember.request(10L, 7L, ShopRole.PARTNER_ADMIN, Set.of(1L), T0).getRequestedBrandIds())
            .isEmpty();
        assertThat(pending().getRequestedBrandIds()).isEmpty();
    }

    @Test
    void 재신청하면_희망_브랜드가_교체되고_역할이_바뀌면_비워진다() {
        ShopAdminMember m = ShopAdminMember.of(1L, 10L, 7L, null, ShopRole.BRAND_ADMIN, Set.of(1L),
            ShopAdminStatus.REJECTED, T0, T1, 99L, "사유");

        m.reapply(7L, ShopRole.BRAND_ADMIN, Set.of(2L, 3L), T1.plusDays(1));
        assertThat(m.getRequestedBrandIds()).containsExactlyInAnyOrder(2L, 3L);

        m.reject("사유", 99L, T1.plusDays(2));
        m.reapply(7L, ShopRole.PARTNER_ADMIN, Set.of(2L), T1.plusDays(3));
        assertThat(m.getRequestedBrandIds()).isEmpty();
    }

    @Test
    void 승인해도_희망_브랜드는_변하지_않고_복원_시_null은_빈_집합이다() {
        ShopAdminMember m = ShopAdminMember.request(10L, 7L, ShopRole.BRAND_ADMIN, Set.of(1L), T0);
        m.approve(ShopRole.BRAND_ADMIN, 99L, T1);

        assertThat(m.getRequestedBrandIds()).containsExactly(1L);
        assertThat(ShopAdminMember.of(1L, 10L, 7L, null, ShopRole.BRAND_ADMIN, null, ShopAdminStatus.PENDING,
            T0, null, null, null).getRequestedBrandIds()).isEmpty();
    }
}
