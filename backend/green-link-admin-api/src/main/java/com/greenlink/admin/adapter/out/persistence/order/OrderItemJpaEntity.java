package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.order.model.OrderItem;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items", indexes = @Index(name = "idx_order_items_order_id", columnList = "order_id"))
public class OrderItemJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private long unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private int sortOrder;

    protected OrderItemJpaEntity() {}

    public static OrderItemJpaEntity of(Long orderId, OrderItem item, int sortOrder) {
        OrderItemJpaEntity e = new OrderItemJpaEntity();
        e.orderId = orderId;
        e.productId = item.getProductId();
        e.productName = item.getProductName();
        e.unitPrice = item.getUnitPrice();
        e.quantity = item.getQuantity();
        e.amount = item.getAmount();
        e.sortOrder = sortOrder;
        return e;
    }

    public OrderItem toDomain() {
        return OrderItem.of(id, orderId, productId, productName, unitPrice, quantity, amount, sortOrder);
    }

    public Long getOrderId() { return orderId; }
}
