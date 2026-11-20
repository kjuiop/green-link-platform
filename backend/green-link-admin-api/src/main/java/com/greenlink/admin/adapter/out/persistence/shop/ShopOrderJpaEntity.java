package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.model.ShopOrderStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_shop_orders_no", columnNames = "order_no"),
        indexes = @Index(name = "idx_shop_orders_company_status", columnList = "company_id, status, created_at"))
public class ShopOrderJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shopId;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 30)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopOrderStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String itemsJson;

    @Column(nullable = false)
    private long totalPrice;

    @Column(nullable = false, length = 50)
    private String ordererName;

    @Column(nullable = false, length = 20)
    private String ordererPhone;

    @Column(nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhone;

    @Column(nullable = false)
    private String address;

    private String addressDetail;

    @Column(length = 10)
    private String deliveryDate;

    @Column(length = 50)
    private String deliveryTime;

    @Column(length = 20)
    private String eventType;

    @Column(length = 100)
    private String placeName;

    @Column(length = 100)
    private String ribbonLeft;

    @Column(length = 200)
    private String ribbonRight;

    @Column(length = 500)
    private String memo;

    @Column(length = 200)
    private String rejectReason;

    private Long linkedOrderId;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    protected ShopOrderJpaEntity() {}

    static ShopOrderJpaEntity from(ShopOrder o) {
        ShopOrderJpaEntity e = new ShopOrderJpaEntity();
        e.shopId = o.getShopId();
        e.companyId = o.getCompanyId();
        e.orderNo = o.getOrderNo();
        e.itemsJson = ShopItemsJson.write(o.getItems());
        e.totalPrice = o.getTotalPrice();
        e.ordererName = o.getOrdererName();
        e.ordererPhone = o.getOrdererPhone();
        e.recipientName = o.getRecipientName();
        e.recipientPhone = o.getRecipientPhone();
        e.address = o.getAddress();
        e.addressDetail = o.getAddressDetail();
        e.deliveryDate = o.getDeliveryDate();
        e.deliveryTime = o.getDeliveryTime();
        e.eventType = o.getEventType();
        e.placeName = o.getPlaceName();
        e.ribbonLeft = o.getRibbonLeft();
        e.ribbonRight = o.getRibbonRight();
        e.memo = o.getMemo();
        e.applyState(o);
        return e;
    }

    void applyState(ShopOrder o) {
        this.status = o.getStatus();
        this.rejectReason = o.getRejectReason();
        this.linkedOrderId = o.getLinkedOrderId();
        this.approvedBy = o.getApprovedBy();
        this.approvedAt = o.getApprovedAt();
    }

    ShopOrder toDomain() {
        ShopOrder.Contact contact = new ShopOrder.Contact(ordererName, ordererPhone, recipientName, recipientPhone,
                address, addressDetail, deliveryDate, deliveryTime, eventType, placeName, ribbonLeft, ribbonRight,
                memo);
        return ShopOrder.of(id, shopId, companyId, orderNo, status, ShopItemsJson.read(itemsJson), totalPrice,
                contact, rejectReason, linkedOrderId, approvedBy, approvedAt, getCreatedAt());
    }
}
