package com.greenlink.core.domain.customerorder.model;

import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.customerorder.CustomerOrderErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerOrder {

    private Long id;
    private Long companyId;
    private OrderChannel channel;
    private CustomerOrderStatus status;
    private String ordererName;
    private String ordererPhone;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverAddressDetail;
    private String deliveryMessage;
    private long totalPrice;
    private LocalDateTime orderedAt;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private CoupangMeta coupangMeta;

    private CustomerOrder() {}

    public static CustomerOrder fromCoupangSheet(Long companyId, CoupangOrderSheet s) {
        CustomerOrder o = new CustomerOrder();
        o.companyId = companyId;
        o.channel = OrderChannel.COUPANG;
        o.status = CustomerOrderStatus.PENDING;
        o.ordererName = s.ordererName();
        o.ordererPhone = s.ordererPhone();
        o.receiverName = s.receiverName();
        o.receiverPhone = s.receiverPhone();
        o.receiverAddress = s.receiverAddress();
        o.receiverAddressDetail = s.receiverAddressDetail();
        o.deliveryMessage = s.deliveryMessage();
        o.totalPrice = s.totalPrice();
        o.orderedAt = s.orderedAt();
        o.coupangMeta = new CoupangMeta(
                null,
                s.orderId(),
                s.shipmentBoxId(),
                s.status(),
                s.items().stream().map(i -> new Item(i.name(), i.quantity(), i.salesPrice())).toList(),
                s.rawJson()
        );
        return o;
    }

    public static CustomerOrder of(Long id, Long companyId, OrderChannel channel, CustomerOrderStatus status,
                                   String ordererName, String ordererPhone,
                                   String receiverName, String receiverPhone,
                                   String receiverAddress, String receiverAddressDetail,
                                   String deliveryMessage, long totalPrice, LocalDateTime orderedAt,
                                   Long approvedBy, LocalDateTime approvedAt, LocalDateTime createdAt,
                                   CoupangMeta coupangMeta) {
        CustomerOrder o = new CustomerOrder();
        o.id = id;
        o.companyId = companyId;
        o.channel = channel;
        o.status = status;
        o.ordererName = ordererName;
        o.ordererPhone = ordererPhone;
        o.receiverName = receiverName;
        o.receiverPhone = receiverPhone;
        o.receiverAddress = receiverAddress;
        o.receiverAddressDetail = receiverAddressDetail;
        o.deliveryMessage = deliveryMessage;
        o.totalPrice = totalPrice;
        o.orderedAt = orderedAt;
        o.approvedBy = approvedBy;
        o.approvedAt = approvedAt;
        o.createdAt = createdAt;
        o.coupangMeta = coupangMeta;
        return o;
    }

    public boolean isApprovable() {
        return status == CustomerOrderStatus.PENDING || status == CustomerOrderStatus.HELD;
    }

    public void approve(Long adminId, LocalDateTime now) {
        if (!isApprovable()) {
            throw new BusinessException(CustomerOrderErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = CustomerOrderStatus.APPROVED;
        this.approvedBy = adminId;
        this.approvedAt = now;
    }

    public void hold() {
        if (status != CustomerOrderStatus.PENDING) {
            throw new BusinessException(CustomerOrderErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = CustomerOrderStatus.HELD;
    }

    public void release() {
        if (status != CustomerOrderStatus.HELD) {
            throw new BusinessException(CustomerOrderErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = CustomerOrderStatus.PENDING;
    }

    public void markCancelled() {
        if (!isApprovable()) {
            throw new BusinessException(CustomerOrderErrorCode.INVALID_STATE_TRANSITION);
        }
        this.status = CustomerOrderStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public OrderChannel getChannel() { return channel; }
    public CustomerOrderStatus getStatus() { return status; }
    public String getOrdererName() { return ordererName; }
    public String getOrdererPhone() { return ordererPhone; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public String getReceiverAddressDetail() { return receiverAddressDetail; }
    public String getDeliveryMessage() { return deliveryMessage; }
    public long getTotalPrice() { return totalPrice; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public Long getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public CoupangMeta getCoupangMeta() { return coupangMeta; }

    public record CoupangMeta(
            Long detailId,
            long coupangOrderId,
            long coupangShipmentBoxId,
            String coupangStatus,
            List<Item> items,
            String rawJson
    ) {}

    public record Item(String name, int quantity, long salesPrice) {}
}
