package com.greenlink.admin.adapter.out.persistence.customerorder;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.admin.adapter.out.persistence.coupang.CoupangItemsJson;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import jakarta.persistence.*;

@Entity
@Table(name = "coupang_order_details",
        uniqueConstraints = @UniqueConstraint(name = "uk_coupang_order_details_order_shipment",
                columnNames = {"coupang_order_id", "coupang_shipment_box_id"}))
public class CoupangOrderDetailJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_order_id", nullable = false)
    private CustomerOrderJpaEntity customerOrder;

    @Column(nullable = false)
    private long coupangOrderId;

    @Column(nullable = false)
    private long coupangShipmentBoxId;

    @Column(length = 30)
    private String coupangStatus;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String rawJson;

    protected CoupangOrderDetailJpaEntity() {}

    static CoupangOrderDetailJpaEntity from(CustomerOrderJpaEntity orderEntity, CustomerOrder.CoupangMeta meta) {
        CoupangOrderDetailJpaEntity e = new CoupangOrderDetailJpaEntity();
        e.customerOrder = orderEntity;
        e.coupangOrderId = meta.coupangOrderId();
        e.coupangShipmentBoxId = meta.coupangShipmentBoxId();
        e.coupangStatus = cut(meta.coupangStatus(), 30);
        e.itemsJson = CoupangItemsJson.writeItems(meta.items());
        e.rawJson = meta.rawJson();
        return e;
    }

    CustomerOrder.CoupangMeta toCoupangMeta() {
        return new CustomerOrder.CoupangMeta(
                id,
                coupangOrderId,
                coupangShipmentBoxId,
                coupangStatus,
                CoupangItemsJson.readItems(itemsJson),
                rawJson
        );
    }

    private static String cut(String value, int max) {
        return value != null && value.length() > max ? value.substring(0, max) : value;
    }

    Long getId() { return id; }
    long getCoupangOrderId() { return coupangOrderId; }
    long getCoupangShipmentBoxId() { return coupangShipmentBoxId; }
}
