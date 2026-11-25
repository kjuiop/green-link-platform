package com.greenlink.shopcore.domain.member;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.global.exception.ErrorCode;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

        m.approve(99L, T1);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(m.getProcessedAt()).isEqualTo(T1);
        assertThat(m.getProcessedBy()).isEqualTo(99L);
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
        assertCode(() -> in(ShopAdminStatus.APPROVED).approve(99L, T1), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        assertCode(() -> in(ShopAdminStatus.REJECTED).approve(99L, T1), ShopErrorCode.ACCESS_INVALID_TRANSITION);
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
}
