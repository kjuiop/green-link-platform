package com.greenlink.core.application.order.service;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderItemCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.order.dto.UpdateOrderCommand;
import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.application.point.usecase.ApplyOrderPointUseCase;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderItem;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderItemRepository;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.domain.orderpriceadjustment.port.OrderPriceAdjustmentRepository;
import com.greenlink.core.domain.photo.port.PhotoRepository;
import com.greenlink.core.domain.point.model.OrderPointState;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import com.greenlink.core.domain.referencephoto.port.ReferencePhotoRepository;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock PhotoRepository photoRepository;
    @Mock ReceiptConfirmationRepository receiptConfirmationRepository;
    @Mock RecordOrderHistoryUseCase recordOrderHistoryUseCase;
    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock ReferencePhotoRepository referencePhotoRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock ApplyOrderPointUseCase applyOrderPointUseCase;
    @Mock OrderItemPricer orderItemPricer;
    @Mock OrderPriceAdjustmentRepository priceAdjustmentRepository;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, orderItemRepository, photoRepository,
                receiptConfirmationRepository, recordOrderHistoryUseCase,
                adminRepository, companyRepository,
                smsMessageRepository, referencePhotoRepository, eventPublisher,
                applyOrderPointUseCase, orderItemPricer, priceAdjustmentRepository);
    }

    private CreateOrderCommand sampleCommand() {
        return new CreateOrderCommand(
                1L, null, null, "장미꽃다발", 1, 50000L, null,
                null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );
    }

    private Order savedOrder(Long id, OrderStatus status) {
        return Order.of(
                id, "DF-20261022-0001", 1L, null, null, status,
                null, "장미꽃다발", 1, 50000L, null, null, 50000L,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, null, null, "2026-10-22T10:00:00", null, null, false, null
        );
    }

    private void stubPricer(long fee, long... unitPrices) {
        given(orderItemPricer.price(any(), any())).willAnswer(inv -> {
            List<OrderItemCommand> cmds = inv.getArgument(1);
            List<OrderItem> items = new java.util.ArrayList<>();
            long qty = 0;
            for (int i = 0; i < cmds.size(); i++) {
                OrderItemCommand c = cmds.get(i);
                items.add(OrderItem.create(c.productId(), c.productName(), unitPrices[i], c.quantity()));
                qty += c.quantity();
            }
            return new OrderItemPricer.Priced(items, fee * qty);
        });
    }

    @Test
    void createOrder_savesOrderAndRecordsHistory() {
        stubPricer(0L, 50000L);
        Order saved = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.save(any(Order.class))).willReturn(saved);
        given(adminRepository.findById(1L)).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        OrderResult result = service.createOrder(sampleCommand());

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo("PENDING");
        verify(recordOrderHistoryUseCase).record(eq(1L), eq(OrderHistoryAction.CREATED), any());
    }

    @Test
    void createOrder_withItems_savesItemsAndSetsRepresentativeName() {
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(null, "장미꽃다발", 50000L, 1),
                new OrderItemCommand(null, "축하화환", 70000L, 2)
        );
        CreateOrderCommand cmd = new CreateOrderCommand(
                1L, null, null, null, 0, 0L, items,
                null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );
        stubPricer(0L, 50000L, 70000L);
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> withId(inv.getArgument(0), 1L));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        OrderResult result = service.createOrder(cmd);

        assertThat(result.productName()).isEqualTo("장미꽃다발 외 1건");
        assertThat(result.amount()).isEqualTo(190000L);
        assertThat(result.totalAmount()).isEqualTo(190000L);
        verify(orderItemRepository).replaceAll(any(), argThat(list -> list.size() == 2));
    }

    @Test
    void getOrder_whenNotFound_throwsOrderNotFound() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    void updateOrderStatus_accept_changesStatusToAccepted() {
        Order pending = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pending));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        OrderResult result = service.updateOrderStatus(1L, "accept");

        assertThat(result.status()).isEqualTo("ACCEPTED");
        verify(recordOrderHistoryUseCase).record(eq(1L), eq(OrderHistoryAction.STATUS_CHANGED), any());
        verify(eventPublisher).publishEvent(new com.greenlink.core.application.fax.event.OrderFaxRequestedEvent(1L));
    }

    @Test
    void updateOrderStatus_reject_changesStatusToRejected() {
        Order pending = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pending));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        OrderResult result = service.updateOrderStatus(1L, "reject");

        assertThat(result.status()).isEqualTo("REJECTED");
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void updateOrderStatus_cancel_changesStatusToCancelled() {
        Order accepted = savedOrder(1L, OrderStatus.ACCEPTED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(accepted));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        OrderResult result = service.updateOrderStatus(1L, "cancel");

        assertThat(result.status()).isEqualTo("CANCELLED");
    }

    @Test
    void updateOrderStatus_complete_whenNoPhoto_throwsSitePhotoRequired() {
        Order accepted = savedOrder(1L, OrderStatus.ACCEPTED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(accepted));
        given(receiptConfirmationRepository.existsWithPhotosByOrderId(1L)).willReturn(false);

        assertThatThrownBy(() -> service.updateOrderStatus(1L, "complete"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.SITE_PHOTO_REQUIRED));
    }

    @Test
    void updateOrderStatus_complete_whenPhotoExists_setsDelivered() {
        Order accepted = savedOrder(1L, OrderStatus.ACCEPTED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(accepted));
        given(receiptConfirmationRepository.existsWithPhotosByOrderId(1L)).willReturn(true);
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        OrderResult result = service.updateOrderStatus(1L, "complete");

        assertThat(result.status()).isEqualTo("DELIVERED");
        assertThat(result.completedAt()).isNotNull();
    }

    @Test
    void updateOrderStatus_invalidAction_throwsInvalidOrderAction() {
        Order pending = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.updateOrderStatus(1L, "approve"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.INVALID_ORDER_ACTION));
    }

    private UpdateOrderCommand updateCommand(int quantity, long amount) {
        return new UpdateOrderCommand(
                1L, null, "장미꽃다발", quantity, amount, null, null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );
    }

    @Test
    void updateOrder_whenSelectionUnchanged_keepsStoredAmountAndVendorFee() {
        Order pending = savedOrder(1L, OrderStatus.PENDING);
        pending.assignVendorFee(3000L);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pending));
        stubToResult();

        OrderResult result = service.updateOrder(1L, updateCommand(1, 999999L));

        verify(orderItemPricer, never()).price(any(), any());
        assertThat(result.amount()).isEqualTo(50000L);
        assertThat(result.vendorFee()).isEqualTo(3000L);
    }

    @Test
    void updateOrder_whenQuantityChanged_repricesAndRecalculatesVendorFee() {
        stubPricer(3000L, 53000L);
        Order pending = savedOrder(1L, OrderStatus.PENDING);
        pending.assignVendorFee(3000L);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pending));
        stubToResult();

        OrderResult result = service.updateOrder(1L, updateCommand(2, 1L));

        assertThat(result.amount()).isEqualTo(106000L);
        assertThat(result.vendorFee()).isEqualTo(6000L);
    }

    @Test
    void updateOrder_withNewItems_replacesItemsAndRecordsHistory() {
        stubPricer(0L, 30000L);
        Order pending = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pending));
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());

        UpdateOrderCommand cmd = new UpdateOrderCommand(
                1L, null, "장미꽃다발", 1, 50000L,
                List.of(new OrderItemCommand(null, "수정된상품", 30000L, 2)),
                null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );

        OrderResult result = service.updateOrder(1L, cmd);

        assertThat(result.productName()).isEqualTo("수정된상품");
        verify(orderItemRepository).replaceAll(eq(1L), argThat(list -> list.size() == 1));
        verify(recordOrderHistoryUseCase).record(eq(1L), eq(OrderHistoryAction.UPDATED),
                argThat(detail -> detail.contains("상품")));
    }

    @Test
    void updateOrder_delivered_whenTotalWouldChange_isBlocked() {
        stubPricer(0L, 50000L);
        given(orderRepository.findById(1L)).willReturn(Optional.of(savedOrder(1L, OrderStatus.DELIVERED)));

        assertThatThrownBy(() -> service.updateOrder(1L, updateCommand(2, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.DELIVERED_AMOUNT_LOCKED));
    }

    @Test
    void updateOrder_delivered_whenSettled_throwsSettledLockedError() {
        stubPricer(0L, 50000L);
        given(orderRepository.findById(1L)).willReturn(Optional.of(savedOrder(1L, OrderStatus.DELIVERED)));
        given(applyOrderPointUseCase.isSettled(1L)).willReturn(true);

        assertThatThrownBy(() -> service.updateOrder(1L, updateCommand(2, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.SETTLED_ORDER_AMOUNT_LOCKED));
    }

    @Test
    void updateOrder_delivered_withAdjustment_keepsTotalWhenOnlyNonAmountFieldsChange() {
        Order delivered = savedOrder(1L, OrderStatus.DELIVERED);
        delivered.applyAdjustment(10000L);
        given(orderRepository.findById(1L)).willReturn(Optional.of(delivered));
        stubToResult();

        OrderResult result = service.updateOrder(1L, updateCommand(1, 50000L));

        assertThat(result.totalAmount()).isEqualTo(60000L);
        assertThat(result.adjustAmount()).isEqualTo(10000L);
    }

    @Test
    void pendingAdjustment_vendorView_subtractsFeeButKeepsDiffAndReason() {
        var view = new com.greenlink.core.application.order.dto.PendingAdjustment(
                1L, "요청자", false, "t", 50000L, 10000L, 60000L, "사유").toVendorView(3000L);

        assertThat(view.beforeTotal()).isEqualTo(47000L);
        assertThat(view.afterTotal()).isEqualTo(57000L);
        assertThat(view.adjustAmount()).isEqualTo(10000L);
        assertThat(view.reason()).isEqualTo("사유");
    }

    @Test
    void toOrderingView_hidesOnlyVendorAmount() {
        Order o = Order.of(
                1L, "DF-20261022-0001", 1L, null, ORDERING_COMPANY_ID, OrderStatus.PENDING,
                null, "장미꽃다발", 1, 50000L, 40000L, null, 50000L,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, null, null, "2026-10-22T10:00:00", null, null, false, null);
        given(orderRepository.findById(1L)).willReturn(Optional.of(o));
        stubToResultReads();

        OrderResult full = service.getOrder(1L);
        OrderResult view = full.toOrderingView();

        assertThat(full.vendorAmount()).isEqualTo(40000L);
        assertThat(view.vendorAmount()).isNull();
        assertThat(view.totalAmount()).isEqualTo(full.totalAmount());
        assertThat(view.orderNumber()).isEqualTo(full.orderNumber());
        assertThat(full.withVendorAmount(7000L).vendorAmount()).isEqualTo(7000L);
    }

    @Test
    void updateCommand_withVendorAmount_replacesOnlyVendorAmount() {
        UpdateOrderCommand cmd = new UpdateOrderCommand(
                1L, null, "상품", 1, 50000L, List.of(), 99999L, 1000L,
                "2026-10-23", "FIXED", "14", "00", null, "주소", null, null, null, null, null,
                null, null, null, null, null, null, null, false, false, null, null, null, null, null);

        UpdateOrderCommand replaced = cmd.withVendorAmount(40000L);

        assertThat(replaced.vendorAmount()).isEqualTo(40000L);
        assertThat(replaced.extraCost()).isEqualTo(1000L);
        assertThat(replaced.vendorId()).isEqualTo(1L);
    }

    @Test
    void getOrders_returnsResultList() {
        Order o = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.search(any(OrderSearchCondition.class))).willReturn(List.of(o));
        given(adminRepository.findAllVendors()).willReturn(List.of());
        given(smsMessageRepository.findAllByOrderIdIn(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findAllByOrderIds(any())).willReturn(List.of());
        given(orderItemRepository.findAllByOrderIds(any())).willReturn(Map.of());

        List<OrderResult> results = service.getOrders(OrderSearchCondition.of(null, null, null, null, null, null, null));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(1L);
        assertThat(results.get(0).items()).hasSize(1);
        assertThat(results.get(0).items().get(0).productName()).isEqualTo("장미꽃다발");
    }

    private static final Long ORDERING_COMPANY_ID = 7L;

    private Order orderingOrder(Long id, OrderStatus status) {
        return Order.of(
                id, "DF-20261022-0001", 1L, null, ORDERING_COMPANY_ID, status,
                null, "장미꽃다발", 1, 50000L, null, null, 50000L,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, null, null, "2026-10-22T10:00:00", null, null, false, null
        );
    }

    private void stubToResult() {
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        stubToResultReads();
    }

    private void stubToResultReads() {
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());
    }

    @Test
    void createOrder_withOrderingCompany_holdsPoint() {
        stubPricer(0L, 50000L);
        CreateOrderCommand cmd = new CreateOrderCommand(
                1L, ORDERING_COMPANY_ID, null, "장미꽃다발", 1, 50000L, null,
                null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );
        given(orderRepository.save(any(Order.class))).willReturn(orderingOrder(1L, OrderStatus.PENDING));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        service.createOrder(cmd);

        verify(applyOrderPointUseCase).holdForNewOrder(eq(ORDERING_COMPANY_ID), eq(1L), any(), eq(50000L), any());
    }

    @Test
    void createOrder_whenPointInsufficient_propagatesAndPublishesNoEvent() {
        stubPricer(0L, 50000L);
        CreateOrderCommand cmd = new CreateOrderCommand(
                1L, ORDERING_COMPANY_ID, null, "장미꽃다발", 1, 50000L, null,
                null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );
        given(orderRepository.save(any(Order.class))).willReturn(orderingOrder(1L, OrderStatus.PENDING));
        org.mockito.Mockito.doThrow(new BusinessException(com.greenlink.core.global.exception.point.PointErrorCode.INSUFFICIENT_POINT))
                .when(applyOrderPointUseCase).holdForNewOrder(anyLong(), anyLong(), any(), anyLong(), any());

        assertThatThrownBy(() -> service.createOrder(cmd)).isInstanceOf(BusinessException.class);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void createOrder_withoutOrderingCompany_doesNotTouchPoint() {
        stubPricer(0L, 50000L);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder(1L, OrderStatus.PENDING));
        given(adminRepository.findById(1L)).willReturn(Optional.empty());
        given(smsMessageRepository.findAllByOrderId(any())).willReturn(List.of());
        given(referencePhotoRepository.countByOrderIds(any())).willReturn(Map.of());
        given(receiptConfirmationRepository.findByOrderId(any())).willReturn(Optional.empty());
        given(orderItemRepository.findAllByOrderId(any())).willReturn(List.of());

        service.createOrder(sampleCommand());

        verify(applyOrderPointUseCase, never()).holdForNewOrder(anyLong(), anyLong(), any(), anyLong(), any());
    }

    @Test
    void updateOrderStatus_cancel_syncsPointAsClosed() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.ACCEPTED)));
        stubToResult();

        service.updateOrderStatus(1L, "cancel");

        verify(applyOrderPointUseCase).syncOrder(eq(ORDERING_COMPANY_ID), eq(1L), any(),
                eq(OrderPointState.CLOSED), eq(50000L), any());
    }

    @Test
    void updateOrderStatus_reject_syncsPointAsClosed() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.PENDING)));
        stubToResult();

        service.updateOrderStatus(1L, "reject");

        verify(applyOrderPointUseCase).syncOrder(eq(ORDERING_COMPANY_ID), eq(1L), any(),
                eq(OrderPointState.CLOSED), eq(50000L), any());
    }

    @Test
    void updateOrderStatus_accept_syncsPointAsActive() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.PENDING)));
        stubToResult();

        service.updateOrderStatus(1L, "accept");

        verify(applyOrderPointUseCase).syncOrder(eq(ORDERING_COMPANY_ID), eq(1L), any(),
                eq(OrderPointState.ACTIVE), eq(50000L), any());
    }

    @Test
    void updateOrderStatus_complete_syncsPointAsDelivered() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.ACCEPTED)));
        given(receiptConfirmationRepository.existsWithPhotosByOrderId(1L)).willReturn(true);
        stubToResult();

        service.updateOrderStatus(1L, "complete");

        verify(applyOrderPointUseCase).syncOrder(eq(ORDERING_COMPANY_ID), eq(1L), any(),
                eq(OrderPointState.DELIVERED), eq(50000L), any());
    }

    @Test
    void deleteOrder_beforeDelivery_closesPointAndMarksDeleted() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.ACCEPTED)));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        service.deleteOrder(1L);

        verify(orderRepository).save(argThat(Order::isDeleted));
        verify(applyOrderPointUseCase).syncOrder(eq(ORDERING_COMPANY_ID), eq(1L), any(),
                eq(OrderPointState.CLOSED), anyLong(), any());
        verify(applyOrderPointUseCase, never()).restoreSettledOrder(any(), any(), any(), any());
        verify(recordOrderHistoryUseCase).record(eq(1L), eq(OrderHistoryAction.STATUS_CHANGED),
                argThat(d -> d.contains("주문서 삭제")));
    }

    @Test
    void deleteOrder_afterDelivery_restoresSettledPoint() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.DELIVERED)));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        service.deleteOrder(1L);

        verify(orderRepository).save(argThat(Order::isDeleted));
        verify(applyOrderPointUseCase).restoreSettledOrder(eq(ORDERING_COMPANY_ID), eq(1L), any(), any());
        verify(applyOrderPointUseCase, never()).syncOrder(any(), any(), any(), any(), anyLong(), any());
    }

    @Test
    void deleteOrder_whenPendingAdjustmentExists_isBlocked() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.DELIVERED)));
        given(priceAdjustmentRepository.findPendingByOrderId(1L)).willReturn(Optional.of(
                com.greenlink.core.domain.orderpriceadjustment.model.OrderPriceAdjustment.request(
                        1L, 5L, "요청자", false, "t", 50000L, 60000L, "사유")));

        assertThatThrownBy(() -> service.deleteOrder(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.ORDER_HAS_PENDING_ADJUST));
        verify(orderRepository, never()).save(any(Order.class));
        verify(applyOrderPointUseCase, never()).restoreSettledOrder(any(), any(), any(), any());
    }

    @Test
    void deleteOrder_whenAlreadyDeleted_isNoOp() {
        Order order = orderingOrder(1L, OrderStatus.DELIVERED);
        order.softDelete();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        service.deleteOrder(1L);

        verify(orderRepository, never()).save(any(Order.class));
        verify(applyOrderPointUseCase, never()).restoreSettledOrder(any(), any(), any(), any());
        verify(applyOrderPointUseCase, never()).syncOrder(any(), any(), any(), any(), anyLong(), any());
    }

    @Test
    void mutatingDeletedOrder_isRejectedAsNotFound() {
        Order order = orderingOrder(1L, OrderStatus.ACCEPTED);
        order.softDelete();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateOrderStatus(1L, "complete"))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        for (String action : List.of("accept", "reject", "cancel")) {
            assertThatThrownBy(() -> service.updateOrderStatus(1L, action))
                    .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        }
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateOrder_onDeletedOrder_isRejectedAsNotFound() {
        Order order = orderingOrder(1L, OrderStatus.ACCEPTED);
        order.softDelete();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        UpdateOrderCommand cmd = new UpdateOrderCommand(
                1L, null, "장미꽃다발", 2, 60000L, null, null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );

        assertThatThrownBy(() -> service.updateOrder(1L, cmd))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrder_deletedOrder_isHiddenFromNonSuperAdmin() {
        Order order = orderingOrder(1L, OrderStatus.DELIVERED);
        order.softDelete();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.getOrder(1L, false))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    void getOrder_deletedOrder_isVisibleToSuperAdmin() {
        Order order = orderingOrder(1L, OrderStatus.DELIVERED);
        order.softDelete();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        stubToResultReads();

        OrderResult result = service.getOrder(1L, true);

        assertThat(result.isDeleted()).isTrue();
    }

    @Test
    void getOrder_normalOrder_isVisibleToEveryone() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.PENDING)));
        stubToResultReads();

        assertThat(service.getOrder(1L, false).isDeleted()).isFalse();
        assertThat(service.getOrder(1L, true).isDeleted()).isFalse();
    }

    @Test
    void getOrder_missingOrder_throwsNotFound() {
        given(orderRepository.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(9L, true))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    void updateOrder_whenSettledAndAmountChanged_throwsLocked() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(orderingOrder(1L, OrderStatus.DELIVERED)));
        given(applyOrderPointUseCase.isSettled(1L)).willReturn(true);
        stubPricer(0L, 30000L);
        UpdateOrderCommand cmd = new UpdateOrderCommand(
                1L, null, "장미꽃다발", 2, 60000L, null, null, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );

        assertThatThrownBy(() -> service.updateOrder(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.SETTLED_ORDER_AMOUNT_LOCKED));
    }

    private static Order withId(Order order, Long id) {
        try {
            var field = Order.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(order, id);
            return order;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
