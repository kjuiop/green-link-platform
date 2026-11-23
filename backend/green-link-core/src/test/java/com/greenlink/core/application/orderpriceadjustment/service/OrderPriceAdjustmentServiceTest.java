package com.greenlink.core.application.orderpriceadjustment.service;

import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.application.orderpriceadjustment.dto.PriceAdjustmentResult;
import com.greenlink.core.application.point.usecase.ApplyOrderPointUseCase;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.domain.orderpriceadjustment.model.OrderPriceAdjustment;
import com.greenlink.core.domain.orderpriceadjustment.model.PriceAdjustmentStatus;
import com.greenlink.core.domain.orderpriceadjustment.port.OrderPriceAdjustmentRepository;
import com.greenlink.core.domain.settlement.port.SettlementRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.ErrorCode;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import com.greenlink.core.global.exception.point.PointErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderPriceAdjustmentServiceTest {

    private static final Long ORDER_ID = 1L;
    private static final Long ADJ_ID = 7L;
    private static final Long VENDOR_CO = 10L;
    private static final Long ORDERING_CO = 20L;

    @Mock OrderRepository orderRepository;
    @Mock OrderPriceAdjustmentRepository adjustmentRepository;
    @Mock SettlementRepository settlementRepository;
    @Mock ApplyOrderPointUseCase applyOrderPointUseCase;
    @Mock RecordOrderHistoryUseCase recordOrderHistoryUseCase;
    @Mock AdminRepository adminRepository;

    private OrderPriceAdjustmentService service;

    @BeforeEach
    void setUp() {
        service = new OrderPriceAdjustmentService(orderRepository, adjustmentRepository, settlementRepository,
                applyOrderPointUseCase, recordOrderHistoryUseCase, adminRepository);
    }

    private Order order(OrderStatus status, Long orderingCompanyId) {
        return Order.of(
                ORDER_ID, "DF-20261022-0001", 1L, VENDOR_CO, orderingCompanyId, status,
                null, "장미꽃다발", 1, 50_000L, null, null, 50_000L,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, null, "2026-10-23T15:00:00", "2026-10-22T10:00:00",
                null, null, false, null);
    }

    private void givenOrder(Order order) {
        given(orderRepository.findByIdForUpdate(ORDER_ID)).willReturn(Optional.of(order));
    }

    private OrderPriceAdjustment pending(long before, long after) {
        return OrderPriceAdjustment.of(ADJ_ID, ORDER_ID, 5L, "요청자", false, "2026-10-24T09:00:00",
                before, after - before, after, "사유", PriceAdjustmentStatus.PENDING,
                null, null, false, null, null);
    }

    private void givenSave() {
        given(adjustmentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
    }

    private void assertError(Runnable r, ErrorCode code) {
        assertThatThrownBy(r::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(code));
    }

    // ---- request ----

    @Test
    void request_increase_savesPendingAndRecordsHistory() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(applyOrderPointUseCase.canAdjustSettledOrder(ORDERING_CO, ORDER_ID, 10_000L)).willReturn(true);
        givenSave();

        PriceAdjustmentResult result = service.request(ORDER_ID, 60_000L, "리본 교체", false);

        assertThat(result.adjustAmount()).isEqualTo(10_000L);
        assertThat(result.beforeTotal()).isEqualTo(50_000L);
        assertThat(result.afterTotal()).isEqualTo(60_000L);
        assertThat(result.status()).isEqualTo("PENDING");
        verify(recordOrderHistoryUseCase).record(eq(ORDER_ID), eq(OrderHistoryAction.PRICE_ADJUSTED),
                org.mockito.ArgumentMatchers.contains("+10,000원"));
    }

    @Test
    void request_decrease_isAllowedAndSignedNegative() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(applyOrderPointUseCase.canAdjustSettledOrder(ORDERING_CO, ORDER_ID, -20_000L)).willReturn(true);
        givenSave();

        PriceAdjustmentResult result = service.request(ORDER_ID, 30_000L, "배송 지연 감액", false);

        assertThat(result.adjustAmount()).isEqualTo(-20_000L);
        verify(recordOrderHistoryUseCase).record(eq(ORDER_ID), eq(OrderHistoryAction.PRICE_ADJUSTED),
                org.mockito.ArgumentMatchers.contains("-20,000원"));
    }

    @Test
    void request_toZeroTotal_isAllowed() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(applyOrderPointUseCase.canAdjustSettledOrder(anyLong(), anyLong(), anyLong())).willReturn(true);
        givenSave();

        PriceAdjustmentResult result = service.request(ORDER_ID, 0L, "전액 감액", false);

        assertThat(result.afterTotal()).isZero();
    }

    @Test
    void request_proxy_appendsProxySuffixToHistory() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(applyOrderPointUseCase.canAdjustSettledOrder(anyLong(), anyLong(), anyLong())).willReturn(true);
        givenSave();

        PriceAdjustmentResult result = service.request(ORDER_ID, 60_000L, "사유", true);

        assertThat(result.requestedByProxy()).isTrue();
        verify(recordOrderHistoryUseCase).record(eq(ORDER_ID), eq(OrderHistoryAction.PRICE_ADJUSTED),
                org.mockito.ArgumentMatchers.endsWith("(관리자 대행)"));
    }

    @Test
    void request_notDelivered_isRejected() {
        for (OrderStatus s : List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.REJECTED, OrderStatus.CANCELLED)) {
            givenOrder(order(s, ORDERING_CO));
            assertError(() -> service.request(ORDER_ID, 60_000L, "사유", false), OrderErrorCode.ADJUST_ONLY_DELIVERED);
        }
        verify(adjustmentRepository, never()).save(any());
    }

    @Test
    void request_whenPendingExists_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.of(pending(50_000L, 60_000L)));

        assertError(() -> service.request(ORDER_ID, 70_000L, "사유", false), OrderErrorCode.ADJUST_PENDING_EXISTS);
        verify(adjustmentRepository, never()).save(any());
    }

    @Test
    void request_sameAmount_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());

        assertError(() -> service.request(ORDER_ID, 50_000L, "사유", false), OrderErrorCode.ADJUST_INVALID_AMOUNT);
    }

    @Test
    void request_negativeTotal_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());

        assertError(() -> service.request(ORDER_ID, -1L, "사유", false), OrderErrorCode.ADJUST_INVALID_AMOUNT);
    }

    @Test
    void request_blankReason_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());

        assertError(() -> service.request(ORDER_ID, 60_000L, "  ", false), OrderErrorCode.ADJUST_REASON_REQUIRED);
        assertError(() -> service.request(ORDER_ID, 60_000L, null, false), OrderErrorCode.ADJUST_REASON_REQUIRED);
    }

    @Test
    void request_whenSettlementCompleted_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(settlementRepository.existsCompletedCovering(anyList(), eq("2026-10-23"))).willReturn(true);

        assertError(() -> service.request(ORDER_ID, 60_000L, "사유", false), OrderErrorCode.ADJUST_SETTLED_LOCKED);
    }

    @Test
    void request_whenPointInsufficient_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(applyOrderPointUseCase.canAdjustSettledOrder(ORDERING_CO, ORDER_ID, 10_000L)).willReturn(false);

        assertError(() -> service.request(ORDER_ID, 60_000L, "사유", false), PointErrorCode.INSUFFICIENT_POINT);
        verify(adjustmentRepository, never()).save(any());
    }

    @Test
    void request_withoutOrderingCompany_skipsPointCheck() {
        givenOrder(order(OrderStatus.DELIVERED, null));
        given(adjustmentRepository.findPendingByOrderId(ORDER_ID)).willReturn(Optional.empty());
        givenSave();

        service.request(ORDER_ID, 60_000L, "사유", false);

        verify(applyOrderPointUseCase, never()).canAdjustSettledOrder(anyLong(), anyLong(), anyLong());
    }

    // ---- accept ----

    @Test
    void accept_increase_appliesAmountAndDeductsPoints() {
        Order order = order(OrderStatus.DELIVERED, ORDERING_CO);
        givenOrder(order);
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(50_000L, 60_000L)));
        givenSave();

        PriceAdjustmentResult result = service.accept(ORDER_ID, ADJ_ID, false);

        assertThat(order.getTotalAmount()).isEqualTo(60_000L);
        assertThat(order.getAdjustAmount()).isEqualTo(10_000L);
        assertThat(result.status()).isEqualTo("ACCEPTED");
        verify(orderRepository).save(order);
        verify(applyOrderPointUseCase).adjustSettledOrder(eq(ORDERING_CO), eq(ORDER_ID), eq("DF-20261022-0001"),
                eq(10_000L), any());
        verify(recordOrderHistoryUseCase).record(eq(ORDER_ID), eq(OrderHistoryAction.PRICE_ADJUSTED),
                org.mockito.ArgumentMatchers.startsWith("금액 수정 승인 +10,000원"));
    }

    @Test
    void accept_decrease_appliesNegativeDelta() {
        Order order = order(OrderStatus.DELIVERED, ORDERING_CO);
        givenOrder(order);
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(50_000L, 30_000L)));
        givenSave();

        service.accept(ORDER_ID, ADJ_ID, false);

        assertThat(order.getTotalAmount()).isEqualTo(30_000L);
        verify(applyOrderPointUseCase).adjustSettledOrder(eq(ORDERING_CO), eq(ORDER_ID), anyString(),
                eq(-20_000L), any());
    }

    @Test
    void accept_withoutOrderingCompany_skipsPoints() {
        Order order = order(OrderStatus.DELIVERED, null);
        givenOrder(order);
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(50_000L, 60_000L)));
        givenSave();

        service.accept(ORDER_ID, ADJ_ID, false);

        verify(applyOrderPointUseCase, never()).adjustSettledOrder(anyLong(), anyLong(), anyString(), anyLong(), any());
    }

    @Test
    void accept_alreadyDecided_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        OrderPriceAdjustment done = OrderPriceAdjustment.of(ADJ_ID, ORDER_ID, 5L, "요청자", false, "t",
                50_000L, 10_000L, 60_000L, "사유", PriceAdjustmentStatus.ACCEPTED, 3L, "승인자", false, "t", null);
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(done));

        assertError(() -> service.accept(ORDER_ID, ADJ_ID, false), OrderErrorCode.ADJUST_NOT_PENDING);
        verify(applyOrderPointUseCase, never()).adjustSettledOrder(anyLong(), anyLong(), anyString(), anyLong(), any());
    }

    @Test
    void accept_whenTotalChangedSinceRequest_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(40_000L, 50_000L)));

        assertError(() -> service.accept(ORDER_ID, ADJ_ID, false), OrderErrorCode.ADJUST_TOTAL_CHANGED);
    }

    @Test
    void accept_whenSettlementCompleted_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(50_000L, 60_000L)));
        given(settlementRepository.existsCompletedCovering(anyList(), anyString())).willReturn(true);

        assertError(() -> service.accept(ORDER_ID, ADJ_ID, false), OrderErrorCode.ADJUST_SETTLED_LOCKED);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void accept_adjustmentOfOtherOrder_isNotFound() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        OrderPriceAdjustment other = OrderPriceAdjustment.of(ADJ_ID, 999L, 5L, "요청자", false, "t",
                50_000L, 10_000L, 60_000L, "사유", PriceAdjustmentStatus.PENDING, null, null, false, null, null);
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(other));

        assertError(() -> service.accept(ORDER_ID, ADJ_ID, false), OrderErrorCode.ADJUST_NOT_FOUND);
    }

    @Test
    void accept_proxy_marksDecidedByProxy() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(50_000L, 60_000L)));
        givenSave();

        PriceAdjustmentResult result = service.accept(ORDER_ID, ADJ_ID, true);

        assertThat(result.decidedByProxy()).isTrue();
    }

    // ---- reject ----

    @Test
    void reject_marksRejectedWithoutTouchingOrderOrPoints() {
        Order order = order(OrderStatus.DELIVERED, ORDERING_CO);
        givenOrder(order);
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(50_000L, 60_000L)));
        givenSave();

        PriceAdjustmentResult result = service.reject(ORDER_ID, ADJ_ID, "금액 불일치", false);

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectReason()).isEqualTo("금액 불일치");
        assertThat(order.getTotalAmount()).isEqualTo(50_000L);
        verify(orderRepository, never()).save(any());
        verify(applyOrderPointUseCase, never()).adjustSettledOrder(anyLong(), anyLong(), anyString(), anyLong(), any());
    }

    @Test
    void reject_blankReason_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(pending(50_000L, 60_000L)));

        assertError(() -> service.reject(ORDER_ID, ADJ_ID, " ", false), OrderErrorCode.ADJUST_REASON_REQUIRED);
    }

    @Test
    void reject_alreadyDecided_isRejected() {
        givenOrder(order(OrderStatus.DELIVERED, ORDERING_CO));
        OrderPriceAdjustment done = OrderPriceAdjustment.of(ADJ_ID, ORDER_ID, 5L, "요청자", false, "t",
                50_000L, 10_000L, 60_000L, "사유", PriceAdjustmentStatus.REJECTED, 3L, "승인자", false, "t", "x");
        given(adjustmentRepository.findById(ADJ_ID)).willReturn(Optional.of(done));

        assertError(() -> service.reject(ORDER_ID, ADJ_ID, "또 거절", false), OrderErrorCode.ADJUST_NOT_PENDING);
    }

    @Test
    void anyAction_onDeletedOrOrphanOrder_isNotFound() {
        given(orderRepository.findByIdForUpdate(ORDER_ID)).willReturn(Optional.empty());

        assertError(() -> service.request(ORDER_ID, 60_000L, "사유", false), OrderErrorCode.ORDER_NOT_FOUND);
        assertError(() -> service.accept(ORDER_ID, ADJ_ID, false), OrderErrorCode.ORDER_NOT_FOUND);
        assertError(() -> service.reject(ORDER_ID, ADJ_ID, "r", false), OrderErrorCode.ORDER_NOT_FOUND);
        verify(recordOrderHistoryUseCase, never()).record(anyLong(), any(), anyString());
        org.mockito.Mockito.verifyNoMoreInteractions(applyOrderPointUseCase);
    }
}
