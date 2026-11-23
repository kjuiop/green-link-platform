package com.greenlink.shopcore.domain.member;

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
        return ShopAdminMember.request(10L, "login", "이름", "회사", T0);
    }

    private ShopAdminMember in(ShopAdminStatus status) {
        return ShopAdminMember.of(1L, 10L, "login", "이름", "회사", status, T0, T1, 99L,
            status == ShopAdminStatus.REJECTED ? "사유" : null);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 신청하면_PENDING으로_생성된다() {
        ShopAdminMember m = pending();

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getRequestedAt()).isEqualTo(T0);
        assertThat(m.getProcessedAt()).isNull();
    }

    @Test
    void PENDING은_승인할_수_있다() {
        ShopAdminMember m = pending();

        m.approve(99L, T1);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getProcessedAt()).isEqualTo(T1);
        assertThat(m.getProcessedBy()).isEqualTo(99L);
    }

    @Test
    void PENDING은_사유와_함께_거절할_수_있다() {
        ShopAdminMember m = pending();

        m.reject("  서류 미비  ", 99L, T1);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REJECTED);
        assertThat(m.getRejectReason()).isEqualTo("서류 미비");
        assertThat(m.getProcessedBy()).isEqualTo(99L);
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
    void 거절된_계정은_재신청하면_PENDING이_되고_이전_처리_정보가_초기화된다() {
        ShopAdminMember m = in(ShopAdminStatus.REJECTED);

        m.reapply(T1.plusDays(1));

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getRejectReason()).isNull();
        assertThat(m.getProcessedAt()).isNull();
        assertThat(m.getProcessedBy()).isNull();
        assertThat(m.getRequestedAt()).isEqualTo(T1.plusDays(1));
    }

    @Test
    void 대기중이거나_승인된_계정은_재신청이_거부된다() {
        assertCode(() -> in(ShopAdminStatus.PENDING).reapply(T1), ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        assertCode(() -> in(ShopAdminStatus.APPROVED).reapply(T1), ShopErrorCode.ACCESS_ALREADY_REQUESTED);
    }

    @Test
    void 스냅샷을_갱신해도_상태는_유지된다() {
        ShopAdminMember m = in(ShopAdminStatus.APPROVED);

        m.refreshSnapshot("newLogin", "새이름", "새회사");

        assertThat(m.getLoginId()).isEqualTo("newLogin");
        assertThat(m.getName()).isEqualTo("새이름");
        assertThat(m.getCompanyName()).isEqualTo("새회사");
        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
    }
}
