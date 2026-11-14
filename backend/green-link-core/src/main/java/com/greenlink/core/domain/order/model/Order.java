package com.greenlink.core.domain.order.model;

import java.util.List;

public class Order {

    private Long id;
    private String orderNumber;
    private Long vendorId;
    private Long vendorCompanyId;
    private Long orderingCompanyId;
    private OrderStatus status;
    private Long productId;
    private String productName;
    private int quantity;
    private long amount;
    private Long vendorAmount;
    private Long extraCost;
    private long totalAmount;
    private String deliveryDate;
    private String deliveryTimeType;
    private String deliveryTimeHour;
    private String deliveryTimeMinute;
    private String deliveryTimeCustom;
    private String address;
    private String addressDetail;
    private String eventType;
    private String placeName;
    private String roomName;
    private String eventInfo;
    private String ribbonLeft;
    private String ribbonRight;
    private String ribbonExtras;
    private String ordererName;
    private String ordererPhone;
    private String recipientName;
    private String recipientPhone;
    private boolean sendPhotoToOrderer;
    private boolean sendPhotoToRecipient;
    private String messageSchedule;
    private String scheduledMessageHour;
    private String scheduledMessageMinute;
    private String adminMemo;
    private String vendorMemo;
    private String acceptedAt;
    private String completedAt;
    private String createdAt;
    private Long createdByAdminId;
    private Long updatedByAdminId;
    private boolean isDeleted;

    private Order() {}

    public static Order create(
            Long vendorId,
            Long vendorCompanyId,
            Long orderingCompanyId,
            String orderNumber,
            List<OrderItem> items,
            Long vendorAmount,
            Long extraCost,
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
            Long createdByAdminId
    ) {
        Order o = new Order();
        o.vendorId = vendorId;
        o.vendorCompanyId = vendorCompanyId;
        o.orderingCompanyId = orderingCompanyId;
        o.createdByAdminId = createdByAdminId;
        o.updatedByAdminId = createdByAdminId;
        o.orderNumber = orderNumber;
        o.status = OrderStatus.PENDING;
        o.vendorAmount = vendorAmount;
        o.applyItems(items, extraCost);
        o.deliveryDate = deliveryDate;
        o.deliveryTimeType = deliveryTimeType;
        o.deliveryTimeHour = deliveryTimeHour;
        o.deliveryTimeMinute = deliveryTimeMinute;
        o.deliveryTimeCustom = deliveryTimeCustom;
        o.address = address;
        o.addressDetail = addressDetail;
        o.eventType = eventType;
        o.placeName = placeName;
        o.roomName = roomName;
        o.eventInfo = eventInfo;
        o.ribbonLeft = ribbonLeft;
        o.ribbonRight = ribbonRight;
        o.ribbonExtras = ribbonExtras;
        o.ordererName = ordererName;
        o.ordererPhone = ordererPhone;
        o.recipientName = recipientName;
        o.recipientPhone = recipientPhone;
        o.sendPhotoToOrderer = sendPhotoToOrderer;
        o.sendPhotoToRecipient = sendPhotoToRecipient;
        o.messageSchedule = messageSchedule;
        o.scheduledMessageHour = scheduledMessageHour;
        o.scheduledMessageMinute = scheduledMessageMinute;
        o.adminMemo = adminMemo;
        o.vendorMemo = vendorMemo;
        return o;
    }

    public static Order of(
            Long id,
            String orderNumber,
            Long vendorId,
            Long vendorCompanyId,
            Long orderingCompanyId,
            OrderStatus status,
            Long productId,
            String productName,
            int quantity,
            long amount,
            Long vendorAmount,
            Long extraCost,
            Long totalAmount,
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
            Long createdByAdminId,
            Long updatedByAdminId,
            boolean isDeleted
    ) {
        Order o = new Order();
        o.id = id;
        o.orderNumber = orderNumber;
        o.vendorId = vendorId;
        o.vendorCompanyId = vendorCompanyId;
        o.orderingCompanyId = orderingCompanyId;
        o.createdByAdminId = createdByAdminId;
        o.updatedByAdminId = updatedByAdminId;
        o.status = status;
        o.productId = productId;
        o.productName = productName;
        o.quantity = quantity;
        o.amount = amount;
        o.vendorAmount = vendorAmount;
        o.extraCost = extraCost;
        o.totalAmount = totalAmount != null ? totalAmount : amount + (extraCost != null ? extraCost : 0L);
        o.deliveryDate = deliveryDate;
        o.deliveryTimeType = deliveryTimeType;
        o.deliveryTimeHour = deliveryTimeHour;
        o.deliveryTimeMinute = deliveryTimeMinute;
        o.deliveryTimeCustom = deliveryTimeCustom;
        o.address = address;
        o.addressDetail = addressDetail;
        o.eventType = eventType;
        o.placeName = placeName;
        o.roomName = roomName;
        o.eventInfo = eventInfo;
        o.ribbonLeft = ribbonLeft;
        o.ribbonRight = ribbonRight;
        o.ribbonExtras = ribbonExtras;
        o.ordererName = ordererName;
        o.ordererPhone = ordererPhone;
        o.recipientName = recipientName;
        o.recipientPhone = recipientPhone;
        o.sendPhotoToOrderer = sendPhotoToOrderer;
        o.sendPhotoToRecipient = sendPhotoToRecipient;
        o.messageSchedule = messageSchedule;
        o.scheduledMessageHour = scheduledMessageHour;
        o.scheduledMessageMinute = scheduledMessageMinute;
        o.adminMemo = adminMemo;
        o.vendorMemo = vendorMemo;
        o.acceptedAt = acceptedAt;
        o.completedAt = completedAt;
        o.createdAt = createdAt;
        o.isDeleted = isDeleted;
        return o;
    }

    public void accept(String acceptedAt) {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.REJECTED) {
            throw new IllegalStateException("발주대기 또는 수주거절 상태에서만 수주승인이 가능합니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.ACCEPTED;
        this.acceptedAt = acceptedAt;
    }

    public void reject() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("발주대기 상태에서만 수주거절이 가능합니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.REJECTED;
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("발주대기 또는 수주완료 상태에서만 주문취소가 가능합니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void complete(String completedAt) {
        if (this.status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("수주완료 상태에서만 배송완료 처리가 가능합니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.DELIVERED;
        this.completedAt = completedAt;
    }

    public void resetToPending() {
        if (this.status != OrderStatus.REJECTED && this.status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("수주거절 또는 수주완료 상태에서만 발주대기로 복귀할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.PENDING;
    }

    private void applyItems(List<OrderItem> items, Long extraCost) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 상품은 1개 이상이어야 합니다.");
        }
        OrderItem first = items.get(0);
        boolean single = items.size() == 1;
        this.productId = single ? first.getProductId() : null;
        this.productName = single ? first.getProductName() : representativeName(first.getProductName(), items.size() - 1);
        this.quantity = items.stream().mapToInt(OrderItem::getQuantity).sum();
        this.amount = items.stream().mapToLong(OrderItem::getAmount).sum();
        this.extraCost = extraCost;
        this.totalAmount = this.amount + (extraCost != null ? extraCost : 0L);
    }

    private static String representativeName(String firstName, int others) {
        String suffix = " 외 " + others + "건";
        int max = 100 - suffix.length();
        String head = firstName.length() > max ? firstName.substring(0, max) : firstName;
        return head + suffix;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void updateInfo(
            Long vendorId,
            Long vendorCompanyId,
            List<OrderItem> items,
            Long vendorAmount,
            Long extraCost,
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
            Long updatedByAdminId
    ) {
        this.vendorId = vendorId;
        this.vendorCompanyId = vendorCompanyId;
        this.updatedByAdminId = updatedByAdminId;
        this.vendorAmount = vendorAmount;
        applyItems(items, extraCost);
        this.deliveryDate = deliveryDate;
        this.deliveryTimeType = deliveryTimeType;
        this.deliveryTimeHour = deliveryTimeHour;
        this.deliveryTimeMinute = deliveryTimeMinute;
        this.deliveryTimeCustom = deliveryTimeCustom;
        this.address = address;
        this.addressDetail = addressDetail;
        this.eventType = eventType;
        this.placeName = placeName;
        this.roomName = roomName;
        this.eventInfo = eventInfo;
        this.ribbonLeft = ribbonLeft;
        this.ribbonRight = ribbonRight;
        this.ribbonExtras = ribbonExtras;
        this.ordererName = ordererName;
        this.ordererPhone = ordererPhone;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.sendPhotoToOrderer = sendPhotoToOrderer;
        this.sendPhotoToRecipient = sendPhotoToRecipient;
        this.messageSchedule = messageSchedule;
        this.scheduledMessageHour = scheduledMessageHour;
        this.scheduledMessageMinute = scheduledMessageMinute;
        this.adminMemo = adminMemo;
        this.vendorMemo = vendorMemo;
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Long getVendorId() { return vendorId; }
    public Long getVendorCompanyId() { return vendorCompanyId; }
    public Long getOrderingCompanyId() { return orderingCompanyId; }
    public OrderStatus getStatus() { return status; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public long getAmount() { return amount; }
    public Long getVendorAmount() { return vendorAmount; }
    public Long getExtraCost() { return extraCost; }
    public long getTotalAmount() { return totalAmount; }
    public String getDeliveryDate() { return deliveryDate; }
    public String getDeliveryTimeType() { return deliveryTimeType; }
    public String getDeliveryTimeHour() { return deliveryTimeHour; }
    public String getDeliveryTimeMinute() { return deliveryTimeMinute; }
    public String getDeliveryTimeCustom() { return deliveryTimeCustom; }
    public String getAddress() { return address; }
    public String getAddressDetail() { return addressDetail; }
    public String getEventType() { return eventType; }
    public String getPlaceName() { return placeName; }
    public String getRoomName() { return roomName; }
    public String getEventInfo() { return eventInfo; }
    public String getRibbonLeft() { return ribbonLeft; }
    public String getRibbonRight() { return ribbonRight; }
    public String getRibbonExtras() { return ribbonExtras; }
    public String getOrdererName() { return ordererName; }
    public String getOrdererPhone() { return ordererPhone; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public boolean isSendPhotoToOrderer() { return sendPhotoToOrderer; }
    public boolean isSendPhotoToRecipient() { return sendPhotoToRecipient; }
    public String getMessageSchedule() { return messageSchedule; }
    public String getScheduledMessageHour() { return scheduledMessageHour; }
    public String getScheduledMessageMinute() { return scheduledMessageMinute; }
    public String getAdminMemo() { return adminMemo; }
    public String getVendorMemo() { return vendorMemo; }
    public String getAcceptedAt() { return acceptedAt; }
    public String getCompletedAt() { return completedAt; }
    public String getCreatedAt() { return createdAt; }
    public Long getCreatedByAdminId() { return createdByAdminId; }
    public Long getUpdatedByAdminId() { return updatedByAdminId; }
    public boolean isDeleted() { return isDeleted; }
}
