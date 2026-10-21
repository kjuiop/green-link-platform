package com.greenlink.core.domain.order.model;

public record OrderSearchCondition(
        String status,
        Long vendorId,
        String orderNumber,
        String productName,
        String ordererName,
        String deliveryDateFrom,
        String deliveryDateTo
) {
    public static OrderSearchCondition of(
            String status,
            Long vendorId,
            String orderNumber,
            String productName,
            String ordererName,
            String deliveryDateFrom,
            String deliveryDateTo
    ) {
        return new OrderSearchCondition(status, vendorId, orderNumber, productName, ordererName, deliveryDateFrom, deliveryDateTo);
    }
}
