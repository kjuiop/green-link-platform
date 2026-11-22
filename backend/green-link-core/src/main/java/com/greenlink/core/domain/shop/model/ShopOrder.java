package com.greenlink.core.domain.shop.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public class ShopOrder {

    private Long id;
    private Long shopId;
    private Long companyId;
    private String orderNo;
    private ShopOrderStatus status;
    private List<ShopOrderItem> items;
    private long totalPrice;
    private String ordererName;
    private String ordererPhone;
    private String recipientName;
    private String recipientPhone;
    private String address;
    private String addressDetail;
    private String deliveryDate;
    private String deliveryTime;
    private String eventType;
    private String placeName;
    private String ribbonLeft;
    private String ribbonRight;
    private String memo;
    private String rejectReason;
    private Long linkedOrderId;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    private ShopOrder() {}

    public record Contact(String ordererName, String ordererPhone, String recipientName, String recipientPhone,
                          String address, String addressDetail, String deliveryDate, String deliveryTime,
                          String eventType, String placeName, String ribbonLeft, String ribbonRight, String memo) {}

    public static ShopOrder place(Shop shop, String orderNo, List<ShopOrderItem> items, Contact c) {
        ShopOrder o = new ShopOrder();
        o.shopId = shop.getId();
        o.companyId = shop.getCompanyId();
        o.orderNo = orderNo;
        o.status = ShopOrderStatus.PENDING;
        o.items = List.copyOf(items);
        o.totalPrice = items.stream().mapToLong(ShopOrderItem::amount).sum();
        o.ordererName = c.ordererName();
        o.ordererPhone = c.ordererPhone();
        o.recipientName = c.recipientName();
        o.recipientPhone = c.recipientPhone();
        o.address = c.address();
        o.addressDetail = c.addressDetail();
        o.deliveryDate = c.deliveryDate();
        o.deliveryTime = c.deliveryTime();
        o.eventType = c.eventType();
        o.placeName = c.placeName();
        o.ribbonLeft = c.ribbonLeft();
        o.ribbonRight = c.ribbonRight();
        o.memo = c.memo();
        return o;
    }

    public static ShopOrder of(Long id, Long shopId, Long companyId, String orderNo, ShopOrderStatus status,
                               List<ShopOrderItem> items, long totalPrice, Contact c, String rejectReason,
                               Long linkedOrderId, Long approvedBy, LocalDateTime approvedAt,
                               LocalDateTime createdAt) {
        ShopOrder o = new ShopOrder();
        o.id = id;
        o.shopId = shopId;
        o.companyId = companyId;
        o.orderNo = orderNo;
        o.status = status;
        o.items = items;
        o.totalPrice = totalPrice;
        o.ordererName = c.ordererName();
        o.ordererPhone = c.ordererPhone();
        o.recipientName = c.recipientName();
        o.recipientPhone = c.recipientPhone();
        o.address = c.address();
        o.addressDetail = c.addressDetail();
        o.deliveryDate = c.deliveryDate();
        o.deliveryTime = c.deliveryTime();
        o.eventType = c.eventType();
        o.placeName = c.placeName();
        o.ribbonLeft = c.ribbonLeft();
        o.ribbonRight = c.ribbonRight();
        o.memo = c.memo();
        o.rejectReason = rejectReason;
        o.linkedOrderId = linkedOrderId;
        o.approvedBy = approvedBy;
        o.approvedAt = approvedAt;
        o.createdAt = createdAt;
        return o;
    }

    public boolean isApprovable() {
        return status == ShopOrderStatus.PENDING || status == ShopOrderStatus.HELD;
    }

    public void approve(Long linkedOrderId, Long adminId, LocalDateTime now) {
        if (!isApprovable()) {
            throw new BusinessException(ShopErrorCode.NOT_APPROVABLE);
        }
        this.status = ShopOrderStatus.APPROVED;
        this.linkedOrderId = linkedOrderId;
        this.approvedBy = adminId;
        this.approvedAt = now;
    }

    public void hold() {
        if (status != ShopOrderStatus.PENDING) {
            throw new BusinessException(ShopErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = ShopOrderStatus.HELD;
    }

    public void release() {
        if (status != ShopOrderStatus.HELD) {
            throw new BusinessException(ShopErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = ShopOrderStatus.PENDING;
    }

    public void reject(String reason) {
        if (!isApprovable()) {
            throw new BusinessException(ShopErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = ShopOrderStatus.REJECTED;
        this.rejectReason = reason == null || reason.isBlank() ? null
                : reason.trim().substring(0, Math.min(reason.trim().length(), 200));
    }

    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public Long getCompanyId() { return companyId; }
    public String getOrderNo() { return orderNo; }
    public ShopOrderStatus getStatus() { return status; }
    public List<ShopOrderItem> getItems() { return items; }
    public long getTotalPrice() { return totalPrice; }
    public String getOrdererName() { return ordererName; }
    public String getOrdererPhone() { return ordererPhone; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getAddress() { return address; }
    public String getAddressDetail() { return addressDetail; }
    public String getDeliveryDate() { return deliveryDate; }
    public String getDeliveryTime() { return deliveryTime; }
    public String getEventType() { return eventType; }
    public String getPlaceName() { return placeName; }
    public String getRibbonLeft() { return ribbonLeft; }
    public String getRibbonRight() { return ribbonRight; }
    public String getMemo() { return memo; }
    public String getRejectReason() { return rejectReason; }
    public Long getLinkedOrderId() { return linkedOrderId; }
    public Long getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
