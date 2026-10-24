package com.greenlink.admin.adapter.out.persistence.orderhistory;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.orderhistory.model.OrderHistory;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import jakarta.persistence.*;

@Entity
@Table(name = "order_histories",
       indexes = @Index(name = "idx_order_histories_order_id", columnList = "order_id"))
public class OrderHistoryJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderHistoryAction action;

    @Column(length = 500)
    private String detail;

    @Column(name = "actor_company", length = 100)
    private String actorCompany;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    protected OrderHistoryJpaEntity() {}

    public static OrderHistoryJpaEntity from(OrderHistory h) {
        OrderHistoryJpaEntity e = new OrderHistoryJpaEntity();
        e.id = h.getId();
        e.orderId = h.getOrderId();
        e.action = h.getAction();
        e.detail = h.getDetail();
        e.actorCompany = h.getActorCompany();
        e.actorName = h.getActorName();
        return e;
    }

    public OrderHistory toDomain() {
        return OrderHistory.of(
                id, orderId, action, detail, actorCompany, actorName,
                getCreatedAt() != null ? getCreatedAt().toString() : null
        );
    }
}
