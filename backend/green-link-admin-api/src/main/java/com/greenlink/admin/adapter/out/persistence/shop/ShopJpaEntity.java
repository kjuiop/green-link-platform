package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.shop.model.Shop;
import jakarta.persistence.*;

@Entity
@Table(name = "shops",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shops_company", columnNames = "company_id"),
                @UniqueConstraint(name = "uk_shops_slug", columnNames = "slug")
        })
public class ShopJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 30)
    private String slug;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 7)
    private String themeColor;

    @Column(length = 500)
    private String bannerUrl;

    @Column(length = 100)
    private String bannerTitle;

    @Column(length = 200)
    private String bannerSubtitle;

    @Column(length = 1000)
    private String notice;

    @Column(length = 2000)
    private String deliveryGuide;

    @Column(length = 1000)
    private String businessInfo;

    protected ShopJpaEntity() {}

    static ShopJpaEntity from(Shop s) {
        ShopJpaEntity e = new ShopJpaEntity();
        e.companyId = s.getCompanyId();
        e.apply(s);
        return e;
    }

    void apply(Shop s) {
        this.slug = s.getSlug();
        this.published = s.isPublished();
        this.displayName = s.getDisplayName();
        this.logoUrl = s.getLogoUrl();
        this.phone = s.getPhone();
        this.themeColor = s.getThemeColor();
        this.bannerUrl = s.getBannerUrl();
        this.bannerTitle = s.getBannerTitle();
        this.bannerSubtitle = s.getBannerSubtitle();
        this.notice = s.getNotice();
        this.deliveryGuide = s.getDeliveryGuide();
        this.businessInfo = s.getBusinessInfo();
    }

    Shop toDomain() {
        return Shop.of(id, companyId, slug, published, displayName, logoUrl, phone, themeColor, bannerUrl,
                bannerTitle, bannerSubtitle, notice, deliveryGuide, businessInfo);
    }
}
