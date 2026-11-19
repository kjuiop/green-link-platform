package com.greenlink.core.domain.coupang.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoupangOrderTest {

    private CoupangOrder newOrder() {
        CoupangOrderSheet sheet = new CoupangOrderSheet(1L, 10L, "ACCEPT", LocalDateTime.now(), "홍길동", "010",
                "김철수", "010", "서울", "101호", "문 앞", List.of(new CoupangOrderSheet.Item("화환", 1, 50000)),
                50000, "{}");
        return CoupangOrder.fromSheet(5L, sheet);
    }

    @Test
    void 신규_주문은_PENDING이고_승인하면_APPROVED로_연결주문이_기록된다() {
        CoupangOrder order = newOrder();
        assertThat(order.getStatus()).isEqualTo(CoupangOrderStatus.PENDING);

        order.approve(99L, 7L, LocalDateTime.now());

        assertThat(order.getStatus()).isEqualTo(CoupangOrderStatus.APPROVED);
        assertThat(order.getLinkedOrderId()).isEqualTo(99L);
        assertThat(order.getApprovedBy()).isEqualTo(7L);
    }

    @Test
    void 이미_승인된_주문은_다시_승인할_수_없다() {
        CoupangOrder order = newOrder();
        order.approve(99L, 7L, LocalDateTime.now());

        assertThatThrownBy(() -> order.approve(100L, 7L, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CoupangErrorCode.NOT_APPROVABLE);
    }

    @Test
    void 보류와_해제는_PENDING_HELD를_오간다() {
        CoupangOrder order = newOrder();
        order.hold();
        assertThat(order.getStatus()).isEqualTo(CoupangOrderStatus.HELD);
        assertThat(order.isApprovable()).isTrue();

        order.release();
        assertThat(order.getStatus()).isEqualTo(CoupangOrderStatus.PENDING);
    }

    @Test
    void 보류가_아닌_주문은_해제할_수_없다() {
        assertThatThrownBy(() -> newOrder().release()).isInstanceOf(BusinessException.class);
    }

    @Test
    void 취소된_주문은_승인할_수_없다() {
        CoupangOrder order = newOrder();
        order.markCanceled();

        assertThat(order.isApprovable()).isFalse();
        assertThatThrownBy(() -> order.approve(1L, 1L, LocalDateTime.now())).isInstanceOf(BusinessException.class);
    }
}
