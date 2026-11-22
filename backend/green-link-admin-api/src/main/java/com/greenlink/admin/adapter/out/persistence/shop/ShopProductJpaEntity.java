package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.shop.model.ShopProduct;
import com.greenlink.core.domain.shop.model.ShopProductCategory;
import jakarta.persistence.*;

@Entity
@Table(name = "shop_products",
        indexes = @Index(name = "idx_shop_products_shop", columnList = "shop_id, visible, sort_order"))
public class ShopProductJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shopId;

    @Column(nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopProductCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private long price;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean visible;

    @Column(nullable = false)
    private int sortOrder;

    protected ShopProductJpaEntity() {}

    static ShopProductJpaEntity from(ShopProduct p) {
        ShopProductJpaEntity e = new ShopProductJpaEntity();
        e.shopId = p.getShopId();
        e.companyId = p.getCompanyId();
        e.apply(p);
        return e;
    }

    void apply(ShopProduct p) {
        this.category = p.getCategory();
        this.name = p.getName();
        this.description = p.getDescription();
        this.price = p.getPrice();
        this.imageUrl = p.getImageUrl();
        this.visible = p.isVisible();
        this.sortOrder = p.getSortOrder();
    }

    ShopProduct toDomain() {
        return ShopProduct.of(id, shopId, companyId, category, name, description, price, imageUrl, visible, sortOrder);
    }
}
