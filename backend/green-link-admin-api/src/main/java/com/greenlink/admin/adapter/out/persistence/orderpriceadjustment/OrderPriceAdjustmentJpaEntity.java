package com.greenlink.admin.adapter.out.persistence.orderpriceadjustment;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.orderpriceadjustment.model.OrderPriceAdjustment;
import com.greenlink.core.domain.orderpriceadjustment.model.PriceAdjustmentStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "order_price_adjustments",
       indexes = @Index(name = "idx_order_price_adjustments_order_id", columnList = "order_id"),
       uniqueConstraints = @UniqueConstraint(name = "uk_order_price_adjustments_pending_order", columnNames = "pending_order_id"))
public class OrderPriceAdjustmentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "pending_order_id")
    private Long pendingOrderId;

    @Column(name = "requester_id")
    private Long requesterId;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    @Column(name = "requested_by_proxy", nullable = false)
    private boolean requestedByProxy;

    @Column(name = "requested_at", length = 30)
    private String requestedAt;

    @Column(name = "before_total", nullable = false)
    private long beforeTotal;

    @Column(name = "adjust_amount", nullable = false)
    private long adjustAmount;

    @Column(name = "after_total", nullable = false)
    private long afterTotal;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceAdjustmentStatus status;

    @Column(name = "decided_by_id")
    private Long decidedById;

    @Column(name = "decided_by_name", length = 100)
    private String decidedByName;

    @Column(name = "decided_by_proxy", nullable = false)
    private boolean decidedByProxy;

    @Column(name = "decided_at", length = 30)
    private String decidedAt;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected OrderPriceAdjustmentJpaEntity() {}

    public static OrderPriceAdjustmentJpaEntity from(OrderPriceAdjustment a) {
        OrderPriceAdjustmentJpaEntity e = new OrderPriceAdjustmentJpaEntity();
        e.id = a.getId();
        e.orderId = a.getOrderId();
        e.pendingOrderId = a.isPending() ? a.getOrderId() : null;
        e.requesterId = a.getRequesterId();
        e.requesterName = a.getRequesterName();
        e.requestedByProxy = a.isRequestedByProxy();
        e.requestedAt = a.getRequestedAt();
        e.beforeTotal = a.getBeforeTotal();
        e.adjustAmount = a.getAdjustAmount();
        e.afterTotal = a.getAfterTotal();
        e.reason = a.getReason();
        e.status = a.getStatus();
        e.decidedById = a.getDecidedById();
        e.decidedByName = a.getDecidedByName();
        e.decidedByProxy = a.isDecidedByProxy();
        e.decidedAt = a.getDecidedAt();
        e.rejectReason = a.getRejectReason();
        return e;
    }

    public OrderPriceAdjustment toDomain() {
        return OrderPriceAdjustment.of(id, orderId, requesterId, requesterName, requestedByProxy, requestedAt,
                beforeTotal, adjustAmount, afterTotal, reason, status,
                decidedById, decidedByName, decidedByProxy, decidedAt, rejectReason);
    }
}
