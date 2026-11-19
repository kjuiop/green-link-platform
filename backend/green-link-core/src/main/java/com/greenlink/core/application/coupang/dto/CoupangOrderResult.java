package com.greenlink.core.application.coupang.dto;

import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;

import java.time.LocalDateTime;
import java.util.List;

public record CoupangOrderResult(
        Long id,
        Long companyId,
        long orderId,
        long shipmentBoxId,
        String status,
        String coupangStatus,
        LocalDateTime orderedAt,
        String ordererName,
        String ordererPhone,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        String receiverAddressDetail,
        String deliveryMessage,
        List<Item> items,
        long totalPrice,
        Long linkedOrderId,
        LocalDateTime approvedAt,
        LocalDateTime createdAt
) {
    public record Item(String name, int quantity, long salesPrice) {}

    public static CoupangOrderResult from(CoupangOrder o) {
        List<Item> items = o.getItems() == null ? List.of()
                : o.getItems().stream().map(CoupangOrderResult::toItem).toList();
        return new CoupangOrderResult(o.getId(), o.getCompanyId(), o.getOrderId(), o.getShipmentBoxId(),
                o.getStatus().name(), o.getCoupangStatus(), o.getOrderedAt(), o.getOrdererName(),
                o.getOrdererPhone(), o.getReceiverName(), o.getReceiverPhone(), o.getReceiverAddress(),
                o.getReceiverAddressDetail(), o.getDeliveryMessage(), items, o.getTotalPrice(),
                o.getLinkedOrderId(), o.getApprovedAt(), o.getCreatedAt());
    }

    private static Item toItem(CoupangOrderSheet.Item i) {
        return new Item(i.name(), i.quantity(), i.salesPrice());
    }
}
