package com.greenlink.core.application.order.dto;

import java.util.List;

public record OrderResult(
        Long id,
        String orderNumber,
        Long vendorId,
        Long vendorCompanyId,
        String vendorName,
        String vendorPhone,
        Long orderingCompanyId,
        String orderingCompanyName,
        String orderingCompanyPhone,
        String status,
        Long productId,
        String productName,
        int quantity,
        long amount,
        Long vendorAmount,
        Long extraCost,
        long totalAmount,
        Long vendorFee,
        List<OrderItemResult> items,
        String deliveryDate,
        String deliveryTimeType,
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
        String vendorMemo,
        String acceptedAt,
        String completedAt,
        String createdAt,
        String messageStatus,
        int referencePhotoCount,
        boolean hasReceipt,
        String receiptReceiverName,
        String receiptReceiverRelation,
        boolean isDeleted,
        String messageSentAt,
        Long createdByAdminId,
        Long updatedByAdminId,
        boolean isDelayed,
        String photoToken
) {

    /** 수주사 시점 금액: 수수료를 제외한 상품금액·합계로 치환하고 수수료 정보는 숨긴다. */
    public OrderResult toVendorView() {
        long fee = vendorFee != null ? vendorFee : 0L;
        if (fee <= 0) return withAmounts(amount, totalAmount, items, null);
        long totalQuantity = items == null ? 0 : items.stream().mapToLong(OrderItemResult::quantity).sum();
        long feePerUnit = totalQuantity > 0 ? fee / totalQuantity : 0L;
        List<OrderItemResult> netItems = items == null ? null : items.stream()
                .map(i -> new OrderItemResult(i.id(), i.productId(), i.productName(),
                        i.unitPrice() - feePerUnit, i.quantity(), i.amount() - feePerUnit * i.quantity()))
                .toList();
        return withAmounts(amount - fee, totalAmount - fee, netItems, null);
    }

    private OrderResult withAmounts(long newAmount, long newTotalAmount, List<OrderItemResult> newItems, Long newVendorFee) {
        return new OrderResult(
                id, orderNumber, vendorId, vendorCompanyId, vendorName, vendorPhone,
                orderingCompanyId, orderingCompanyName, orderingCompanyPhone,
                status, productId, productName, quantity, newAmount,
                vendorAmount, extraCost, newTotalAmount, newVendorFee, newItems, deliveryDate, deliveryTimeType, deliveryTimeHour,
                deliveryTimeMinute, deliveryTimeCustom, address, addressDetail,
                eventType, placeName, roomName, eventInfo,
                ribbonLeft, ribbonRight, ribbonExtras,
                ordererName, ordererPhone, recipientName, recipientPhone,
                sendPhotoToOrderer, sendPhotoToRecipient,
                messageSchedule, scheduledMessageHour, scheduledMessageMinute,
                adminMemo, vendorMemo, acceptedAt, completedAt,
                createdAt, messageStatus, referencePhotoCount,
                hasReceipt, receiptReceiverName, receiptReceiverRelation,
                isDeleted, messageSentAt,
                createdByAdminId, updatedByAdminId,
                isDelayed, photoToken);
    }
}
