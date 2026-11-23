package com.greenlink.core.domain.order.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderAdjustAmountTest {

    private Order order(OrderStatus status, long amount, Long extraCost) {
        return Order.of(
                1L, "DF-1", 1L, 10L, 20L, status,
                null, "상품", 1, amount, null, extraCost, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "주소", null, null, null, null, null,
                null, null, null, "주문자", "010-1111-2222",
                "수령자", "010-3333-4444", true, true,
                "SCHEDULED", "13", "00", null, null, null, null, "2026-10-22T10:00:00",
                null, null, false, null);
    }

    @Test
    void applyAdjustment_increase_updatesAdjustAndTotal() {
        Order o = order(OrderStatus.DELIVERED, 50_000L, 3_000L);

        o.applyAdjustment(10_000L);

        assertThat(o.getAdjustAmount()).isEqualTo(10_000L);
        assertThat(o.getTotalAmount()).isEqualTo(63_000L);
        assertThat(o.getExtraCost()).isEqualTo(3_000L);
    }

    @Test
    void applyAdjustment_accumulatesAcrossCalls() {
        Order o = order(OrderStatus.DELIVERED, 50_000L, null);

        o.applyAdjustment(10_000L);
        o.applyAdjustment(-4_000L);

        assertThat(o.getAdjustAmount()).isEqualTo(6_000L);
        assertThat(o.getTotalAmount()).isEqualTo(56_000L);
    }

    @Test
    void applyAdjustment_downToZero_isAllowed() {
        Order o = order(OrderStatus.DELIVERED, 50_000L, null);

        o.applyAdjustment(-50_000L);

        assertThat(o.getTotalAmount()).isZero();
    }

    @Test
    void applyAdjustment_belowZero_isRejected() {
        Order o = order(OrderStatus.DELIVERED, 50_000L, null);

        assertThatThrownBy(() -> o.applyAdjustment(-50_001L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.ADJUST_INVALID_AMOUNT));
        assertThat(o.getTotalAmount()).isEqualTo(50_000L);
    }

    @Test
    void applyAdjustment_nonDelivered_isRejected() {
        for (OrderStatus s : OrderStatus.values()) {
            if (s == OrderStatus.DELIVERED) continue;
            Order o = order(s, 50_000L, null);
            assertThatThrownBy(() -> o.applyAdjustment(1_000L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(OrderErrorCode.ADJUST_ONLY_DELIVERED));
        }
    }

    @Test
    void getAdjustAmount_defaultsToZero() {
        assertThat(order(OrderStatus.DELIVERED, 1L, null).getAdjustAmount()).isZero();
    }
}
