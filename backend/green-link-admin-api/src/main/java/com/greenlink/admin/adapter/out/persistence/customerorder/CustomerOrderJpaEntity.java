package com.greenlink.admin.adapter.out.persistence.customerorder;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_orders",
        indexes = @Index(name = "idx_customer_orders_company_channel_status",
                columnList = "company_id, channel, status, ordered_at"))
public class CustomerOrderJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerOrderStatus status;

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

    @Column(nullable = false)
    private long totalPrice;

    private LocalDateTime orderedAt;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    @OneToOne(mappedBy = "customerOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    private CoupangOrderDetailJpaEntity coupangDetail;

    protected CustomerOrderJpaEntity() {}

    static CustomerOrderJpaEntity from(CustomerOrder o) {
        CustomerOrderJpaEntity e = new CustomerOrderJpaEntity();
        e.companyId = o.getCompanyId();
        e.channel = o.getChannel();
        e.status = o.getStatus();
        e.ordererName = cut(o.getOrdererName(), 100);
        e.ordererPhone = cut(o.getOrdererPhone(), 50);
        e.receiverName = cut(o.getReceiverName(), 100);
        e.receiverPhone = cut(o.getReceiverPhone(), 50);
        e.receiverAddress = cut(o.getReceiverAddress(), 500);
        e.receiverAddressDetail = cut(o.getReceiverAddressDetail(), 500);
        e.deliveryMessage = cut(o.getDeliveryMessage(), 1000);
        e.totalPrice = o.getTotalPrice();
        e.orderedAt = o.getOrderedAt();
        e.approvedBy = o.getApprovedBy();
        e.approvedAt = o.getApprovedAt();
        return e;
    }

    void applyState(CustomerOrder o) {
        this.status = o.getStatus();
        this.approvedBy = o.getApprovedBy();
        this.approvedAt = o.getApprovedAt();
    }

    private static String cut(String value, int max) {
        return value != null && value.length() > max ? value.substring(0, max) : value;
    }

    CustomerOrder toDomain() {
        CustomerOrder.CoupangMeta meta = coupangDetail != null ? coupangDetail.toCoupangMeta() : null;
        return CustomerOrder.of(id, companyId, channel, status,
                ordererName, ordererPhone,
                receiverName, receiverPhone,
                receiverAddress, receiverAddressDetail,
                deliveryMessage, totalPrice, orderedAt,
                approvedBy, approvedAt, getCreatedAt(),
                meta);
    }

    Long getId() { return id; }
    Long getCompanyId() { return companyId; }
}
