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

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "base_product_id")
    private Long baseProductId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(length = 500)
    private String memo;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean enabled = true;

    protected ProductJpaEntity() {}

    public static ProductJpaEntity from(Product product) {
        ProductJpaEntity e = new ProductJpaEntity();
        e.id = product.getId();
        e.companyId = product.getCompanyId();
        e.baseProductId = product.getBaseProductId();
        e.name = product.getName();
        e.price = product.getPrice();
        e.memo = product.getMemo();
        e.sortOrder = product.getSortOrder();
        e.enabled = product.isEnabled();
        return e;
    }

    public Product toDomain() {
        return Product.of(id, companyId, baseProductId, name, price, memo,
                getCreatedAt() != null ? getCreatedAt().toString() : null, sortOrder, enabled);
    }
}
