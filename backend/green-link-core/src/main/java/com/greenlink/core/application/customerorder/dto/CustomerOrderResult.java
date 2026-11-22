package com.greenlink.core.application.customerorder.dto;

import com.greenlink.core.domain.customerorder.model.CustomerOrder;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerOrderResult(
        Long id,
        Long companyId,
        String channel,
        String status,
        String ordererName,
        String ordererPhone,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        String receiverAddressDetail,
        String deliveryMessage,
        long totalPrice,
        LocalDateTime orderedAt,
        Long approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        CoupangMeta coupangMeta
) {
    public record CoupangMeta(
            long coupangOrderId,
            long coupangShipmentBoxId,
            String coupangStatus,
            List<Item> items
    ) {}

    public record Item(String name, int quantity, long salesPrice) {}

    public static CustomerOrderResult from(CustomerOrder o) {
        CoupangMeta meta = null;
        if (o.getCoupangMeta() != null) {
            CustomerOrder.CoupangMeta m = o.getCoupangMeta();
            List<Item> items = m.items() == null ? List.of()
                    : m.items().stream().map(i -> new Item(i.name(), i.quantity(), i.salesPrice())).toList();
            meta = new CoupangMeta(m.coupangOrderId(), m.coupangShipmentBoxId(), m.coupangStatus(), items);
        }
        return new CustomerOrderResult(
                o.getId(), o.getCompanyId(),
                o.getChannel().name(), o.getStatus().name(),
                o.getOrdererName(), o.getOrdererPhone(),
                o.getReceiverName(), o.getReceiverPhone(),
                o.getReceiverAddress(), o.getReceiverAddressDetail(),
                o.getDeliveryMessage(), o.getTotalPrice(),
                o.getOrderedAt(),
                o.getApprovedBy(), o.getApprovedAt(),
                o.getCreatedAt(),
                meta
        );
    }
}
