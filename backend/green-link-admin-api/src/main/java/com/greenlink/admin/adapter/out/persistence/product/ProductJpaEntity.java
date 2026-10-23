package com.greenlink.admin.adapter.out.persistence.product;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.product.model.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class ProductJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(length = 500)
    private String memo;

    protected ProductJpaEntity() {}

    public static ProductJpaEntity from(Product product) {
        ProductJpaEntity e = new ProductJpaEntity();
        e.id = product.getId();
        e.name = product.getName();
        e.price = product.getPrice();
        e.memo = product.getMemo();
        return e;
    }

    public Product toDomain() {
        return Product.of(
                id, name, price, memo,
                getCreatedAt() != null ? getCreatedAt().toString() : null
        );
    }
}
