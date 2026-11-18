package com.greenlink.admin.adapter.out.persistence.product;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.product.model.BaseProduct;
import jakarta.persistence.*;

@Entity
@Table(name = "base_products")
public class BaseProductJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(length = 500)
    private String memo;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected BaseProductJpaEntity() {}

    public static BaseProductJpaEntity from(BaseProduct product) {
        BaseProductJpaEntity e = new BaseProductJpaEntity();
        e.id = product.getId();
        e.name = product.getName();
        e.price = product.getPrice();
        e.memo = product.getMemo();
        e.sortOrder = product.getSortOrder();
        return e;
    }

    public BaseProduct toDomain() {
        return BaseProduct.of(id, name, price, memo,
                getCreatedAt() != null ? getCreatedAt().toString() : null, sortOrder);
    }
}
