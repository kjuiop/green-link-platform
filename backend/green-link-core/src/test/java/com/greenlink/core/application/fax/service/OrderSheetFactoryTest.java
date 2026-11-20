package com.greenlink.core.application.fax.service;

import com.greenlink.core.domain.fax.model.OrderSheet;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderItem;
import com.greenlink.core.domain.order.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSheetFactoryTest {

    private Order order(String timeType, String hour, String minute, String custom, String recipient) {
        return Order.of(
                10L, "DF-1", 1L, 100L, 200L, OrderStatus.ACCEPTED,
                null, "장미", 1, 50000L, null, 5000L, 55000L,
                "2026-11-21", timeType, hour, minute, custom,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "주문자", "010-1234-5678",
                recipient, "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, null, null, "2026-11-20T10:00:00", null, null, false, null);
    }

    @Test
    void 배달주소와_일시를_인쇄_화면과_같은_규칙으로_만든다() {
        OrderSheet sheet = OrderSheetFactory.create(order("까지", "14", "30", null, "김영희"),
                List.of(OrderItem.create(null, "장미", 50000L, 2)), null, null);

        assertThat(sheet.deliveryAddress()).isEqualTo("서울시 강남구 101호 / 그랜드볼룸 A홀");
        assertThat(sheet.deliveryDateTime()).isEqualTo("2026-11-21 14:30까지");
        assertThat(sheet.productSummary()).isEqualTo("장미 × 2");
        assertThat(sheet.totalAmount()).isEqualTo(55000L);
    }

    @Test
    void 받는분이_없으면_주문자를_고객명으로_쓴다() {
        OrderSheet sheet = OrderSheetFactory.create(order("3시간이내", null, null, null, null),
                List.of(OrderItem.create(null, "장미", 50000L, 1)), null, null);

        assertThat(sheet.customerName()).isEqualTo("주문자");
        assertThat(sheet.deliveryDateTime()).isEqualTo("2026-11-21 3시간이내");
    }
}
