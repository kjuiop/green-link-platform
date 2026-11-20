package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.shop.dto.PlaceShopOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaceShopOrderRequest(
        @NotEmpty @Size(max = 10) List<@Valid Line> items,
        @NotBlank @Size(max = 50) String ordererName,
        @NotBlank @Size(max = 30) String ordererPhone,
        @NotBlank @Size(max = 50) String recipientName,
        @NotBlank @Size(max = 30) String recipientPhone,
        @NotBlank @Size(max = 255) String address,
        @Size(max = 255) String addressDetail,
        @NotBlank @Size(max = 10) String deliveryDate,
        @Size(max = 50) String deliveryTime,
        @Size(max = 20) String eventType,
        @Size(max = 100) String placeName,
        @Size(max = 100) String ribbonLeft,
        @Size(max = 200) String ribbonRight,
        @Size(max = 500) String memo,
        @Size(max = 100) String website
) {
    public record Line(@NotNull Long productId, @Min(1) @Max(99) int quantity) {}

    public PlaceShopOrderCommand toCommand() {
        return new PlaceShopOrderCommand(
                items.stream().map(l -> new PlaceShopOrderCommand.Line(l.productId(), l.quantity())).toList(),
                ordererName, ordererPhone, recipientName, recipientPhone, address, addressDetail,
                deliveryDate, deliveryTime, eventType, placeName, ribbonLeft, ribbonRight, memo, website);
    }
}
