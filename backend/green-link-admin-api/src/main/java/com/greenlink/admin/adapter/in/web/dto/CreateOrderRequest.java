package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long vendorId,
        Long orderingCompanyId,
        Long productId,
        String productName,
        Integer quantity,
        Long amount,
        @Valid List<OrderItemRequest> items,
        Long vendorAmount,
        Long extraCost,
        @NotBlank String deliveryDate,
        @NotBlank String deliveryTimeType,
        String deliveryTimeHour,
        String deliveryTimeMinute,
        String deliveryTimeCustom,
        String address,
        String addressDetail,
        String eventType,
        String placeName,
        String roomName,
        String eventInfo,
        String ribbonLeft,
        String ribbonRight,
        String ribbonExtras,
        String ordererName,
        String ordererPhone,
        String recipientName,
        String recipientPhone,
        boolean sendPhotoToOrderer,
        boolean sendPhotoToRecipient,
        String messageSchedule,
        String scheduledMessageHour,
        String scheduledMessageMinute,
        String adminMemo,
        String vendorMemo
) {
    public CreateOrderCommand toCommand(Long orderingCompanyId) {
        return new CreateOrderCommand(
                vendorId,
                orderingCompanyId,
                productId,
                productName,
                quantity != null ? quantity : 0,
                amount != null ? amount : 0L,
                OrderItemRequest.toCommands(items),
                vendorAmount,
                extraCost,
                deliveryDate,
                deliveryTimeType,
                deliveryTimeHour,
                deliveryTimeMinute,
                deliveryTimeCustom,
                address,
                addressDetail,
                eventType,
                placeName,
                roomName,
                eventInfo,
                ribbonLeft,
                ribbonRight,
                ribbonExtras,
                ordererName,
                ordererPhone,
                recipientName,
                recipientPhone,
                sendPhotoToOrderer,
                sendPhotoToRecipient,
                messageSchedule,
                scheduledMessageHour,
                scheduledMessageMinute,
                adminMemo,
                vendorMemo
        );
    }
}
