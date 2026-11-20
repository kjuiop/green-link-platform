package com.greenlink.core.application.shop.dto;

import com.greenlink.core.domain.shop.model.ShopOrder;

import java.time.LocalDateTime;
import java.util.List;

public record ShopOrderResult(
        Long id,
        Long companyId,
        String orderNo,
        String status,
        List<Item> items,
        long totalPrice,
        String ordererName,
        String ordererPhone,
        String recipientName,
        String recipientPhone,
        String address,
        String addressDetail,
        String deliveryDate,
        String deliveryTime,
        String eventType,
        String placeName,
        String ribbonLeft,
        String ribbonRight,
        String memo,
        String rejectReason,
        Long linkedOrderId,
        Long linkedOrderAmount,
        Long margin,
        LocalDateTime approvedAt,
        LocalDateTime createdAt
) {
    public record Item(Long productId, String name, long unitPrice, int quantity) {}

    public static ShopOrderResult from(ShopOrder o) {
        return from(o, null);
    }

    /** linkedOrderAmount: 연결된 발주의 합계(알고 있을 때만). 마진 = 판매가 − 발주금액. */
    public static ShopOrderResult from(ShopOrder o, Long linkedOrderAmount) {
        return new ShopOrderResult(o.getId(), o.getCompanyId(), o.getOrderNo(), o.getStatus().name(),
                o.getItems().stream().map(i -> new Item(i.productId(), i.name(), i.unitPrice(), i.quantity())).toList(),
                o.getTotalPrice(), o.getOrdererName(), o.getOrdererPhone(), o.getRecipientName(),
                o.getRecipientPhone(), o.getAddress(), o.getAddressDetail(), o.getDeliveryDate(),
                o.getDeliveryTime(), o.getEventType(), o.getPlaceName(), o.getRibbonLeft(), o.getRibbonRight(),
                o.getMemo(), o.getRejectReason(), o.getLinkedOrderId(), linkedOrderAmount,
                linkedOrderAmount == null ? null : o.getTotalPrice() - linkedOrderAmount,
                o.getApprovedAt(), o.getCreatedAt());
    }
}
