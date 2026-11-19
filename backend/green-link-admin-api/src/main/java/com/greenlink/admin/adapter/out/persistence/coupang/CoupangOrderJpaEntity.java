package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.model.CoupangOrderStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupang_orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_coupang_orders_company_shipment",
                columnNames = {"company_id", "shipment_box_id"}),
        indexes = @Index(name = "idx_coupang_orders_company_status", columnList = "company_id, status, ordered_at"))
public class CoupangOrderJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private long orderId;

    @Column(nullable = false)
    private long shipmentBoxId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoupangOrderStatus status;

    @Column(length = 30)
    private String coupangStatus;

    private LocalDateTime orderedAt;

    @Column(length = 100)
    private String ordererName;

    @Column(length = 50)
    private String ordererPhone;

    @Column(length = 100)
    private String receiverName;

    @Column(length = 50)
    private String receiverPhone;

    @Column(length = 500)
    private String receiverAddress;

    @Column(length = 500)
    private String receiverAddressDetail;

    @Column(length = 1000)
    private String deliveryMessage;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Column(nullable = false)
    private long totalPrice;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String rawJson;

    private Long linkedOrderId;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    protected CoupangOrderJpaEntity() {}

    static CoupangOrderJpaEntity from(CoupangOrder o) {
        CoupangOrderJpaEntity e = new CoupangOrderJpaEntity();
        e.companyId = o.getCompanyId();
        e.orderId = o.getOrderId();
        e.shipmentBoxId = o.getShipmentBoxId();
        e.orderedAt = o.getOrderedAt();
        e.ordererName = o.getOrdererName();
        e.ordererPhone = o.getOrdererPhone();
        e.receiverName = o.getReceiverName();
        e.receiverPhone = o.getReceiverPhone();
        e.receiverAddress = o.getReceiverAddress();
        e.receiverAddressDetail = o.getReceiverAddressDetail();
        e.deliveryMessage = o.getDeliveryMessage();
        e.itemsJson = CoupangItemsJson.write(o.getItems());
        e.totalPrice = o.getTotalPrice();
        e.rawJson = o.getRawJson();
        e.applyState(o);
        return e;
    }

    void applyState(CoupangOrder o) {
        this.status = o.getStatus();
        this.coupangStatus = o.getCoupangStatus();
        this.linkedOrderId = o.getLinkedOrderId();
        this.approvedBy = o.getApprovedBy();
        this.approvedAt = o.getApprovedAt();
    }

    CoupangOrder toDomain() {
        return CoupangOrder.of(id, companyId, orderId, shipmentBoxId, status, coupangStatus, orderedAt,
                ordererName, ordererPhone, receiverName, receiverPhone, receiverAddress, receiverAddressDetail,
                deliveryMessage, CoupangItemsJson.read(itemsJson), totalPrice, rawJson, linkedOrderId,
                approvedBy, approvedAt, getCreatedAt());
    }
}
