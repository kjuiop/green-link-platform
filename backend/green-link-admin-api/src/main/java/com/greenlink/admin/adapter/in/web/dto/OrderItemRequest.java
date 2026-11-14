package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.order.dto.OrderItemCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderItemRequest(
        Long productId,
        @NotBlank @Size(max = 100) String productName,
        @NotNull @Min(0) Long unitPrice,
        @NotNull @Min(1) Integer quantity
) {
    public OrderItemCommand toCommand() {
        return new OrderItemCommand(productId, productName, unitPrice, quantity);
    }

    public static List<OrderItemCommand> toCommands(List<OrderItemRequest> items) {
        return items == null ? null : items.stream().map(OrderItemRequest::toCommand).toList();
    }
}
