package com.greenlink.core.application.order.service;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.application.order.dto.UpdateOrderCommand;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrdersUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderStatusUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderUseCase;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService implements CreateOrderUseCase, GetOrderUseCase,
        GetOrdersUseCase, UpdateOrderUseCase, UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderResult createOrder(CreateOrderCommand cmd) {
        String orderNumber = "DF-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-"
                + String.format("%04d", System.nanoTime() % 10000);

        Order order = Order.create(
                cmd.vendorId(),
                orderNumber,
                cmd.productName(),
                cmd.quantity(),
                cmd.amount(),
                cmd.extraCost(),
                cmd.deliveryDate(),
                cmd.deliveryTimeType(),
                cmd.deliveryTimeHour(),
                cmd.deliveryTimeMinute(),
                cmd.deliveryTimeCustom(),
                cmd.address(),
                cmd.addressDetail(),
                cmd.eventType(),
                cmd.placeName(),
                cmd.roomName(),
                cmd.eventInfo(),
                cmd.ribbonLeft(),
                cmd.ribbonRight(),
                cmd.ordererName(),
                cmd.ordererPhone(),
                cmd.recipientName(),
                cmd.recipientPhone(),
                cmd.sendPhotoToOrderer(),
                cmd.sendPhotoToRecipient(),
                cmd.messageSchedule(),
                cmd.scheduledMessageHour(),
                cmd.scheduledMessageMinute(),
                cmd.adminMemo(),
                cmd.vendorMemo()
        );

        Order saved = orderRepository.save(order);
        return toResult(saved);
    }

    @Override
    public OrderResult getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return toResult(order);
    }

    @Override
    public List<OrderResult> getOrders(OrderSearchCondition condition) {
        return orderRepository.search(condition).stream().map(this::toResult).toList();
    }

    @Override
    @Transactional
    public OrderResult updateOrder(Long id, UpdateOrderCommand cmd) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateInfo(
                cmd.vendorId(),
                cmd.productName(),
                cmd.quantity(),
                cmd.amount(),
                cmd.extraCost(),
                cmd.deliveryDate(),
                cmd.deliveryTimeType(),
                cmd.deliveryTimeHour(),
                cmd.deliveryTimeMinute(),
                cmd.deliveryTimeCustom(),
                cmd.address(),
                cmd.addressDetail(),
                cmd.eventType(),
                cmd.placeName(),
                cmd.roomName(),
                cmd.eventInfo(),
                cmd.ribbonLeft(),
                cmd.ribbonRight(),
                cmd.ordererName(),
                cmd.ordererPhone(),
                cmd.recipientName(),
                cmd.recipientPhone(),
                cmd.sendPhotoToOrderer(),
                cmd.sendPhotoToRecipient(),
                cmd.messageSchedule(),
                cmd.scheduledMessageHour(),
                cmd.scheduledMessageMinute(),
                cmd.adminMemo(),
                cmd.vendorMemo()
        );

        Order saved = orderRepository.save(order);
        return toResult(saved);
    }

    @Override
    @Transactional
    public OrderResult updateOrderStatus(Long id, String action) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        switch (action.toLowerCase()) {
            case "accept" -> order.accept();
            case "reject" -> order.reject();
            case "cancel" -> order.cancel();
            case "complete" -> order.complete(LocalDateTime.now().toString());
            default -> throw new IllegalArgumentException("유효하지 않은 action입니다: " + action);
        }

        Order saved = orderRepository.save(order);
        return toResult(saved);
    }

    private OrderResult toResult(Order order) {
        return new OrderResult(
                order.getId(),
                order.getOrderNumber(),
                order.getVendorId(),
                null,
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getProductName(),
                order.getQuantity(),
                order.getAmount(),
                order.getExtraCost(),
                order.getDeliveryDate(),
                order.getDeliveryTimeType(),
                order.getDeliveryTimeHour(),
                order.getDeliveryTimeMinute(),
                order.getDeliveryTimeCustom(),
                order.getAddress(),
                order.getAddressDetail(),
                order.getEventType(),
                order.getPlaceName(),
                order.getRoomName(),
                order.getEventInfo(),
                order.getRibbonLeft(),
                order.getRibbonRight(),
                order.getOrdererName(),
                order.getOrdererPhone(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.isSendPhotoToOrderer(),
                order.isSendPhotoToRecipient(),
                order.getMessageSchedule(),
                order.getScheduledMessageHour(),
                order.getScheduledMessageMinute(),
                order.getAdminMemo(),
                order.getVendorMemo(),
                order.getCompletedAt(),
                order.getCreatedAt()
        );
    }
}
