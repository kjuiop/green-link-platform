package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull Long vendorId,
        @NotBlank String productName,
        @NotNull Integer quantity,
        @NotNull Long amount,
        Long extraCost,
        @NotBlank String deliveryDate,
        @NotBlank String deliveryTimeType,
        String deliveryTimeHour,
        String deliveryTimeMinute,
        String deliveryTimeCustom,
        @NotBlank String address,
        String addressDetail,
        String eventType,
        String placeName,
        String roomName,
        String eventInfo,
        String ribbonLeft,
        String ribbonRight,
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
                productName,
                quantity,
                amount,
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
