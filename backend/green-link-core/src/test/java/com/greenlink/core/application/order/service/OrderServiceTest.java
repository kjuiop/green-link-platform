package com.greenlink.core.application.order.service;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.domain.photo.port.PhotoRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock PhotoRepository photoRepository;
    @Mock RecordOrderHistoryUseCase recordOrderHistoryUseCase;
    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, photoRepository,
                recordOrderHistoryUseCase, adminRepository, companyRepository);
    }

    private CreateOrderCommand sampleCommand() {
        return new CreateOrderCommand(
                1L, "장미꽃다발", 1, 50000L, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null
        );
    }

    private Order savedOrder(Long id, OrderStatus status) {
        return Order.of(
                id, "DF-20261022-0001", 1L, status,
                "장미꽃다발", 1, 50000L, null,
                "2026-10-23", "FIXED", "14", "00", null,
                "서울시 강남구", "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, null, "2026-10-22T10:00:00"
        );
    }

    @Test
    void createOrder_savesOrderAndRecordsHistory() {
        Order saved = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.save(any(Order.class))).willReturn(saved);
        given(adminRepository.findById(1L)).willReturn(Optional.empty());

        OrderResult result = service.createOrder(sampleCommand());

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo("PENDING");
        verify(recordOrderHistoryUseCase).record(eq(1L), eq(OrderHistoryAction.CREATED), any());
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

        OrderResult result = service.updateOrderStatus(1L, "accept");

        assertThat(result.status()).isEqualTo("ACCEPTED");
        verify(recordOrderHistoryUseCase).record(eq(1L), eq(OrderHistoryAction.STATUS_CHANGED), any());
    }

    @Test
    void updateOrderStatus_reject_changesStatusToRejected() {
        Order pending = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(pending));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());

        OrderResult result = service.updateOrderStatus(1L, "reject");

        assertThat(result.status()).isEqualTo("REJECTED");
    }

    @Test
    void updateOrderStatus_cancel_changesStatusToCancelled() {
        Order accepted = savedOrder(1L, OrderStatus.ACCEPTED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(accepted));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());

        OrderResult result = service.updateOrderStatus(1L, "cancel");

        assertThat(result.status()).isEqualTo("CANCELLED");
    }

    @Test
    void updateOrderStatus_complete_whenNoPhoto_throwsSitePhotoRequired() {
        Order accepted = savedOrder(1L, OrderStatus.ACCEPTED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(accepted));
        given(photoRepository.existsByOrderId(1L)).willReturn(false);

        assertThatThrownBy(() -> service.updateOrderStatus(1L, "complete"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(OrderErrorCode.SITE_PHOTO_REQUIRED));
    }

    @Test
    void updateOrderStatus_complete_whenPhotoExists_setsDelivered() {
        Order accepted = savedOrder(1L, OrderStatus.ACCEPTED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(accepted));
        given(photoRepository.existsByOrderId(1L)).willReturn(true);
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.findById(anyLong())).willReturn(Optional.empty());

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

    @Test
    void getOrders_returnsResultList() {
        Order o = savedOrder(1L, OrderStatus.PENDING);
        given(orderRepository.search(any(OrderSearchCondition.class))).willReturn(List.of(o));
        given(adminRepository.findAllVendors()).willReturn(List.of());

        List<OrderResult> results = service.getOrders(OrderSearchCondition.of(null, null, null, null, null, null, null));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(1L);
    }
}
