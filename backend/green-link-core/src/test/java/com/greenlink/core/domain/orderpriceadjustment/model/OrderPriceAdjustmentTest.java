package com.greenlink.core.domain.orderpriceadjustment.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderPriceAdjustmentTest {

    private OrderPriceAdjustment request(long before, long after, String reason) {
        return OrderPriceAdjustment.request(1L, 5L, "요청자", false, "2026-10-24T09:00:00", before, after, reason);
    }

    @Test
    void request_computesSignedDiffAndTrimsReason() {
        OrderPriceAdjustment a = request(50_000L, 40_000L, "  감액  ");

        assertThat(a.getAdjustAmount()).isEqualTo(-10_000L);
        assertThat(a.getReason()).isEqualTo("감액");
        assertThat(a.getStatus()).isEqualTo(PriceAdjustmentStatus.PENDING);
    }

    @Test
    void request_reasonOver500_isRejected() {
        assertThatThrownBy(() -> request(1L, 2L, "가".repeat(501)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.ADJUST_REASON_REQUIRED));
    }

    @Test
    void request_reasonExactly500_isAllowed() {
        assertThat(request(1L, 2L, "가".repeat(500)).getReason()).hasSize(500);
    }

    @Test
    void accept_thenAcceptAgain_isRejected() {
        OrderPriceAdjustment a = request(1L, 2L, "사유");
        a.accept(3L, "승인자", false, "t");

        assertThat(a.getStatus()).isEqualTo(PriceAdjustmentStatus.ACCEPTED);
        assertThatThrownBy(() -> a.accept(3L, "승인자", false, "t"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.ADJUST_NOT_PENDING));
    }

    @Test
    void reject_afterAccept_isRejected() {
        OrderPriceAdjustment a = request(1L, 2L, "사유");
        a.accept(3L, "승인자", false, "t");

        assertThatThrownBy(() -> a.reject(3L, "승인자", false, "t", "사유"))
                .isInstanceOf(BusinessException.class);
    }
}
