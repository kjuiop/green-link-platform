package com.greenlink.core.domain.order.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final String ACCEPTED_AT = "2026-10-22 10:00:00";

    private static OrderItem item(String name, long unitPrice, int quantity) {
        return OrderItem.create(null, name, unitPrice, quantity);
    }

    private Order orderWith(List<OrderItem> items, Long extraCost) {
        return Order.create(
                1L, null, null, "DF-20261022-0001", items, null, extraCost,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, 1L
        );
    }

    private Order pendingOrder() {
        return orderWith(List.of(item("장미꽃다발", 50000L, 1)), null);
    }

    @Test
    void create_singleItem_keepsProductInfo() {
        Order order = orderWith(List.of(item("장미꽃다발", 25000L, 2)), null);
        assertThat(order.getProductName()).isEqualTo("장미꽃다발");
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getAmount()).isEqualTo(50000L);
        assertThat(order.getTotalAmount()).isEqualTo(50000L);
    }

    @Test
    void create_multipleItems_sumsAmountAndUsesRepresentativeName() {
        Order order = orderWith(List.of(item("장미꽃다발", 50000L, 1), item("축하화환", 70000L, 2), item("꽃바구니", 30000L, 1)), 10000L);
        assertThat(order.getProductName()).isEqualTo("장미꽃다발 외 2건");
        assertThat(order.getQuantity()).isEqualTo(4);
        assertThat(order.getProductId()).isNull();
        assertThat(order.getAmount()).isEqualTo(220000L);
        assertThat(order.getTotalAmount()).isEqualTo(230000L);
    }

    @Test
    void create_withoutItems_throws() {
        assertThatThrownBy(() -> orderWith(List.of(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderItem_invalidValues_throw() {
        assertThatThrownBy(() -> item("꽃", 1000L, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item("꽃", -1L, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item(" ", 1000L, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item("꽃", Long.MAX_VALUE, 2)).isInstanceOf(IllegalArgumentException.class);
    }

    private Order acceptedOrder() {
        Order o = pendingOrder();
        o.accept(ACCEPTED_AT);
        return o;
    }

    @Test
    void create_setsStatusToPending() {
        Order order = pendingOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void accept_fromPending_setsAccepted() {
        Order order = pendingOrder();
        order.accept(ACCEPTED_AT);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void accept_fromNonPending_throwsIllegalStateException() {
        Order order = acceptedOrder();
        assertThatThrownBy(() -> order.accept(ACCEPTED_AT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("수주승인이 가능합니다");
    }

    @Test
    void reject_fromPending_setsRejected() {
        Order order = pendingOrder();
        order.reject();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void reject_fromAccepted_throwsIllegalStateException() {
        Order order = acceptedOrder();
        assertThatThrownBy(order::reject)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("발주대기 상태에서만");
    }

    @Test
    void cancel_fromPending_setsCancelled() {
        Order order = pendingOrder();
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_fromAccepted_setsCancelled() {
        Order order = acceptedOrder();
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_fromDelivered_throwsIllegalStateException() {
        Order order = acceptedOrder();
        order.complete("2026-10-23T14:00:00");
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("발주대기 또는 수주완료 상태에서만");
    }

    @Test
    void cancel_fromRejected_throwsIllegalStateException() {
        Order order = pendingOrder();
        order.reject();
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void complete_fromAccepted_setsDeliveredAndCompletedAt() {
        Order order = acceptedOrder();
        order.complete("2026-10-23T14:00:00");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getCompletedAt()).isEqualTo("2026-10-23T14:00:00");
    }

    @Test
    void complete_fromPending_throwsIllegalStateException() {
        Order order = pendingOrder();
        assertThatThrownBy(() -> order.complete("2026-10-23T14:00:00"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("수주완료 상태에서만");
    }

    @Test
    void updateInfo_fromAccepted_succeeds() {
        Order order = acceptedOrder();
        order.updateInfo(
                1L, null, List.of(item("수정된상품", 30000L, 2)), null, 5000L,
                "2026-10-24", "FIXED", "15", "00", null,
                "서울시", "102호", "기념일", null, null, null,
                null, null, null, "홍길동", "010-1234-5678", "김영희",
                "010-9876-5432", false, false, null, null, null, null, null, 1L);
        assertThat(order.getAmount()).isEqualTo(60000L);
        assertThat(order.getTotalAmount()).isEqualTo(65000L);
        assertThat(order.getProductName()).isEqualTo("수정된상품");
    }
}
