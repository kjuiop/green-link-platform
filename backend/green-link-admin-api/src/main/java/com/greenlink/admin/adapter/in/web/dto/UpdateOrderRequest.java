package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.order.dto.UpdateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateOrderRequest(
        @NotNull Long vendorId,
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
        @NotBlank String ordererName,
        @NotBlank String ordererPhone,
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
    public UpdateOrderCommand toCommand() {
        return new UpdateOrderCommand(
                vendorId,
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
