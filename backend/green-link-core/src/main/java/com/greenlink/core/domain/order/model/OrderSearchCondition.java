package com.greenlink.core.domain.order.model;

public record OrderSearchCondition(
        String status,
        Long vendorId,
        String orderNumber,
        String productName,
        String ordererName,
        String deliveryDateFrom,
        String deliveryDateTo,
        Long orderingCompanyId,
        String receiverName,
        String messageStatus,
        String ribbonLeft,
        Boolean deleted
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
        return new OrderSearchCondition(status, vendorId, orderNumber, productName, ordererName, deliveryDateFrom, deliveryDateTo, null, null, null, null, null);
    }

    public static OrderSearchCondition of(
            String status,
            Long vendorId,
            String orderNumber,
            String productName,
            String ordererName,
            String deliveryDateFrom,
            String deliveryDateTo,
            Long orderingCompanyId
    ) {
        return new OrderSearchCondition(status, vendorId, orderNumber, productName, ordererName, deliveryDateFrom, deliveryDateTo, orderingCompanyId, null, null, null, null);
    }

    public static OrderSearchCondition of(
            String status,
            Long vendorId,
            String orderNumber,
            String productName,
            String ordererName,
            String deliveryDateFrom,
            String deliveryDateTo,
            Long orderingCompanyId,
            String receiverName,
            String messageStatus,
            String ribbonLeft
    ) {
        return new OrderSearchCondition(status, vendorId, orderNumber, productName, ordererName, deliveryDateFrom, deliveryDateTo, orderingCompanyId, receiverName, messageStatus, ribbonLeft, null);
    }

    public static OrderSearchCondition of(
            String status,
            Long vendorId,
            String orderNumber,
            String productName,
            String ordererName,
            String deliveryDateFrom,
            String deliveryDateTo,
            Long orderingCompanyId,
            String receiverName,
            String messageStatus,
            String ribbonLeft,
            Boolean deleted
    ) {
        return new OrderSearchCondition(status, vendorId, orderNumber, productName, ordererName, deliveryDateFrom, deliveryDateTo, orderingCompanyId, receiverName, messageStatus, ribbonLeft, deleted);
    }
}
