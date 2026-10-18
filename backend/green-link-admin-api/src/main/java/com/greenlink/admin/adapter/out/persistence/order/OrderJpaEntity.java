package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long amount;

    @Column
    private Long extraCost;

    @Column(nullable = false, length = 10)
    private String deliveryDate;

    @Column(nullable = false, length = 20)
    private String deliveryTimeType;

    @Column(length = 5)
    private String deliveryTimeHour;

    @Column(length = 5)
    private String deliveryTimeMinute;

    @Column(length = 100)
    private String deliveryTimeCustom;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255)
    private String addressDetail;

    @Column(length = 20)
    private String eventType;

    @Column(length = 100)
    private String placeName;

    @Column(length = 100)
    private String roomName;

    @Column(length = 255)
    private String eventInfo;

    @Column(length = 100)
    private String ribbonLeft;

    @Column(length = 100)
    private String ribbonRight;

    @Column(nullable = false, length = 50)
    private String ordererName;

    @Column(nullable = false, length = 20)
    private String ordererPhone;

    @Column(length = 50)
    private String recipientName;

    @Column(length = 20)
    private String recipientPhone;

    @Column(nullable = false)
    private boolean sendPhotoToOrderer;

    @Column(nullable = false)
    private boolean sendPhotoToRecipient;

    @Column(length = 20)
    private String messageSchedule;

    @Column(length = 5)
    private String scheduledMessageHour;

    @Column(length = 5)
    private String scheduledMessageMinute;

    @Column(length = 500)
    private String adminMemo;

    @Column(length = 500)
    private String vendorMemo;

    @Column(length = 30)
    private String completedAt;

    protected OrderJpaEntity() {}

    public static OrderJpaEntity from(Order order) {
        OrderJpaEntity e = new OrderJpaEntity();
        e.id = order.getId();
        e.orderNumber = order.getOrderNumber();
        e.vendorId = order.getVendorId();
        e.status = order.getStatus();
        e.productName = order.getProductName();
        e.quantity = order.getQuantity();
        e.amount = order.getAmount();
        e.extraCost = order.getExtraCost();
        e.deliveryDate = order.getDeliveryDate();
        e.deliveryTimeType = order.getDeliveryTimeType();
        e.deliveryTimeHour = order.getDeliveryTimeHour();
        e.deliveryTimeMinute = order.getDeliveryTimeMinute();
        e.deliveryTimeCustom = order.getDeliveryTimeCustom();
        e.address = order.getAddress();
        e.addressDetail = order.getAddressDetail();
        e.eventType = order.getEventType();
        e.placeName = order.getPlaceName();
        e.roomName = order.getRoomName();
        e.eventInfo = order.getEventInfo();
        e.ribbonLeft = order.getRibbonLeft();
        e.ribbonRight = order.getRibbonRight();
        e.ordererName = order.getOrdererName();
        e.ordererPhone = order.getOrdererPhone();
        e.recipientName = order.getRecipientName();
        e.recipientPhone = order.getRecipientPhone();
        e.sendPhotoToOrderer = order.isSendPhotoToOrderer();
        e.sendPhotoToRecipient = order.isSendPhotoToRecipient();
        e.messageSchedule = order.getMessageSchedule();
        e.scheduledMessageHour = order.getScheduledMessageHour();
        e.scheduledMessageMinute = order.getScheduledMessageMinute();
        e.adminMemo = order.getAdminMemo();
        e.vendorMemo = order.getVendorMemo();
        e.completedAt = order.getCompletedAt();
        return e;
    }

    public Order toDomain() {
        return Order.of(
                id,
                orderNumber,
                vendorId,
                status,
                productName,
                quantity,
                amount,
                extraCost,
                deliveryDate,
                deliveryTimeType,
                deliveryTimeHour,
                deliveryTimeMinute,
                deliveryTimeCustom,
                address,
                addressDetail,
                eventType,
                placeName,
                roomName,
                eventInfo,
                ribbonLeft,
                ribbonRight,
                ordererName,
                ordererPhone,
                recipientName,
                recipientPhone,
                sendPhotoToOrderer,
                sendPhotoToRecipient,
                messageSchedule,
                scheduledMessageHour,
                scheduledMessageMinute,
                adminMemo,
                vendorMemo,
                completedAt
        );
    }
}
