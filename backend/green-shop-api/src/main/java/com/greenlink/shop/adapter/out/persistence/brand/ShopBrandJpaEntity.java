package com.greenlink.shop.adapter.out.persistence.brand;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "shop_brands", indexes = @Index(name = "idx_shop_brands_company", columnList = "company_id"))
public class ShopBrandJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 100)
    private String name;

    protected ShopBrandJpaEntity() {}

    static ShopBrandJpaEntity from(ShopBrand b) {
        ShopBrandJpaEntity e = new ShopBrandJpaEntity();
        e.id = b.getId();
        e.companyId = b.getCompanyId();
        e.name = b.getName();
        return e;
    }

    ShopBrand toDomain() {
        return ShopBrand.of(id, companyId, name);
    }
}
