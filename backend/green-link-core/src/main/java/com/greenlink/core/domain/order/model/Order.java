package com.greenlink.core.domain.order.model;

public class Order {

    private Long id;
    private String orderNumber;
    private Long vendorId;
    private Long orderingCompanyId;
    private OrderStatus status;
    private Long productId;
    private String productName;
    private int quantity;
    private long amount;
    private Long extraCost;
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
    private String completedAt;
    private String createdAt;

    private Order() {}

    public static Order create(
            Long vendorId,
            Long orderingCompanyId,
            String orderNumber,
            Long productId,
            String productName,
            int quantity,
            long amount,
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
        Order o = new Order();
        o.vendorId = vendorId;
        o.orderingCompanyId = orderingCompanyId;
        o.orderNumber = orderNumber;
        o.status = OrderStatus.PENDING;
        o.productId = productId;
        o.productName = productName;
        o.quantity = quantity;
        o.amount = amount;
        o.extraCost = extraCost;
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
            Long orderingCompanyId,
            OrderStatus status,
            Long productId,
            String productName,
            int quantity,
            long amount,
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
            String completedAt,
            String createdAt
    ) {
        Order o = new Order();
        o.id = id;
        o.orderNumber = orderNumber;
        o.vendorId = vendorId;
        o.orderingCompanyId = orderingCompanyId;
        o.status = status;
        o.productId = productId;
        o.productName = productName;
        o.quantity = quantity;
        o.amount = amount;
        o.extraCost = extraCost;
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
        o.completedAt = completedAt;
        o.createdAt = createdAt;
        return o;
    }

    public void accept() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("발주대기 상태에서만 수주승인이 가능합니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.ACCEPTED;
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

    public void updateInfo(
            Long vendorId,
            Long productId,
            String productName,
            int quantity,
            long amount,
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
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("발주대기 상태에서만 주문 수정이 가능합니다. 현재 상태: " + this.status);
        }
        this.vendorId = vendorId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.amount = amount;
        this.extraCost = extraCost;
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
    public Long getOrderingCompanyId() { return orderingCompanyId; }
    public OrderStatus getStatus() { return status; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public long getAmount() { return amount; }
    public Long getExtraCost() { return extraCost; }
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
    public String getCompletedAt() { return completedAt; }
    public String getCreatedAt() { return createdAt; }
}
