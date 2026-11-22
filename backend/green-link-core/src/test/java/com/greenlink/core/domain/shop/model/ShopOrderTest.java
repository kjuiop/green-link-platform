package com.greenlink.core.domain.shop.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopOrderTest {

    ShopOrder order;

    @BeforeEach
    void setUp() {
        Shop shop = Shop.of(1L, 7L, "rose", true, "장미꽃집", null, null, "#15803d", null, null, null, null, null, null);
        ShopOrder.Contact contact = new ShopOrder.Contact("홍길동", "01012345678", "김철수", "01099998888",
                "서울시", null, "2099-01-01", null, null, null, null, null, null);
        order = ShopOrder.place(shop, "SO-20990101-ABC234",
                List.of(new ShopOrderItem(1L, "근조3단", 80_000, 2), new ShopOrderItem(2L, "축하3단", 70_000, 1)),
                contact);
    }

    @Test
    void 접수하면_PENDING이고_합계가_계산된다() {
        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.PENDING);
        assertThat(order.getTotalPrice()).isEqualTo(230_000);
        assertThat(order.getCompanyId()).isEqualTo(7L);
    }

    @Test
    void 승인하면_발주ID와_승인자가_기록된다() {
        LocalDateTime now = LocalDateTime.now();
        order.approve(99L, 5L, now);

        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.APPROVED);
        assertThat(order.getLinkedOrderId()).isEqualTo(99L);
        assertThat(order.getApprovedBy()).isEqualTo(5L);
        assertThat(order.getApprovedAt()).isEqualTo(now);
    }

    @Test
    void 보류_해제_후에도_승인할_수_있다() {
        order.hold();
        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.HELD);
        assertThat(order.isApprovable()).isTrue();

        order.release();
        order.approve(1L, 1L, LocalDateTime.now());
        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.APPROVED);
    }

    @Test
    void 이미_승인된_주문은_다시_승인할_수_없다() {
        order.approve(1L, 1L, LocalDateTime.now());

        assertThatThrownBy(() -> order.approve(2L, 1L, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.NOT_APPROVABLE);
    }

    @Test
    void 거절하면_사유가_저장되고_승인할_수_없다() {
        order.reject("  재고 없음 ");

        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.REJECTED);
        assertThat(order.getRejectReason()).isEqualTo("재고 없음");
        assertThat(order.isApprovable()).isFalse();
    }

    @Test
    void 승인된_주문은_보류_거절_할_수_없다() {
        order.approve(1L, 1L, LocalDateTime.now());

        assertThatThrownBy(order::hold).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> order.reject("x")).isInstanceOf(BusinessException.class);
    }

    @Test
    void PENDING이_아니면_해제할_수_없다() {
        assertThatThrownBy(order::release).isInstanceOf(BusinessException.class);
    }
}
