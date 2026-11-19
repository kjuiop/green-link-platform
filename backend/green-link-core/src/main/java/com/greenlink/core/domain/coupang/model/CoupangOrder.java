package com.greenlink.core.domain.coupang.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public class CoupangOrder {

    private Long id;
    private Long companyId;
    private long orderId;
    private long shipmentBoxId;
    private CoupangOrderStatus status;
    private String coupangStatus;
    private LocalDateTime orderedAt;
    private String ordererName;
    private String ordererPhone;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverAddressDetail;
    private String deliveryMessage;
    private List<CoupangOrderSheet.Item> items;
    private long totalPrice;
    private String rawJson;
    private Long linkedOrderId;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    private CoupangOrder() {}

    public static CoupangOrder fromSheet(Long companyId, CoupangOrderSheet s) {
        CoupangOrder o = new CoupangOrder();
        o.companyId = companyId;
        o.orderId = s.orderId();
        o.shipmentBoxId = s.shipmentBoxId();
        o.status = CoupangOrderStatus.PENDING;
        o.coupangStatus = s.status();
        o.orderedAt = s.orderedAt();
        o.ordererName = s.ordererName();
        o.ordererPhone = s.ordererPhone();
        o.receiverName = s.receiverName();
        o.receiverPhone = s.receiverPhone();
        o.receiverAddress = s.receiverAddress();
        o.receiverAddressDetail = s.receiverAddressDetail();
        o.deliveryMessage = s.deliveryMessage();
        o.items = s.items();
        o.totalPrice = s.totalPrice();
        o.rawJson = s.rawJson();
        return o;
    }

    public static CoupangOrder of(Long id, Long companyId, long orderId, long shipmentBoxId,
                                  CoupangOrderStatus status, String coupangStatus, LocalDateTime orderedAt,
                                  String ordererName, String ordererPhone, String receiverName,
                                  String receiverPhone, String receiverAddress, String receiverAddressDetail,
                                  String deliveryMessage, List<CoupangOrderSheet.Item> items, long totalPrice,
                                  String rawJson, Long linkedOrderId, Long approvedBy,
                                  LocalDateTime approvedAt, LocalDateTime createdAt) {
        CoupangOrder o = new CoupangOrder();
        o.id = id;
        o.companyId = companyId;
        o.orderId = orderId;
        o.shipmentBoxId = shipmentBoxId;
        o.status = status;
        o.coupangStatus = coupangStatus;
        o.orderedAt = orderedAt;
        o.ordererName = ordererName;
        o.ordererPhone = ordererPhone;
        o.receiverName = receiverName;
        o.receiverPhone = receiverPhone;
        o.receiverAddress = receiverAddress;
        o.receiverAddressDetail = receiverAddressDetail;
        o.deliveryMessage = deliveryMessage;
        o.items = items;
        o.totalPrice = totalPrice;
        o.rawJson = rawJson;
        o.linkedOrderId = linkedOrderId;
        o.approvedBy = approvedBy;
        o.approvedAt = approvedAt;
        o.createdAt = createdAt;
        return o;
    }

    public boolean isApprovable() {
        return status == CoupangOrderStatus.PENDING || status == CoupangOrderStatus.HELD;
    }

    public void approve(Long linkedOrderId, Long adminId, LocalDateTime now) {
        if (!isApprovable()) {
            throw new BusinessException(CoupangErrorCode.NOT_APPROVABLE);
        }
        this.status = CoupangOrderStatus.APPROVED;
        this.linkedOrderId = linkedOrderId;
        this.approvedBy = adminId;
        this.approvedAt = now;
    }

    public void hold() {
        if (status != CoupangOrderStatus.PENDING) {
            throw new BusinessException(CoupangErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = CoupangOrderStatus.HELD;
    }

    public void release() {
        if (status != CoupangOrderStatus.HELD) {
            throw new BusinessException(CoupangErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = CoupangOrderStatus.PENDING;
    }

    public void markCanceled() {
        if (!isApprovable()) {
            throw new BusinessException(CoupangErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = CoupangOrderStatus.CANCELED;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public long getOrderId() { return orderId; }
    public long getShipmentBoxId() { return shipmentBoxId; }
    public CoupangOrderStatus getStatus() { return status; }
    public String getCoupangStatus() { return coupangStatus; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public String getOrdererName() { return ordererName; }
    public String getOrdererPhone() { return ordererPhone; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public String getReceiverAddressDetail() { return receiverAddressDetail; }
    public String getDeliveryMessage() { return deliveryMessage; }
    public List<CoupangOrderSheet.Item> getItems() { return items; }
    public long getTotalPrice() { return totalPrice; }
    public String getRawJson() { return rawJson; }
    public Long getLinkedOrderId() { return linkedOrderId; }
    public Long getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
