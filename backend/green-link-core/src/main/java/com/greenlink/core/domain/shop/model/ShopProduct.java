package com.greenlink.core.domain.shop.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;

public class ShopProduct {

    public static final long MAX_PRICE = 100_000_000L;

    private Long id;
    private Long shopId;
    private Long companyId;
    private ShopProductCategory category;
    private String name;
    private String description;
    private long price;
    private String imageUrl;
    private boolean visible;
    private int sortOrder;

    private ShopProduct() {}

    public static ShopProduct create(Long shopId, Long companyId, ShopProductCategory category, String name,
                                     String description, long price, String imageUrl, boolean visible,
                                     int sortOrder) {
        ShopProduct p = new ShopProduct();
        p.shopId = shopId;
        p.companyId = companyId;
        p.sortOrder = sortOrder;
        p.update(category, name, description, price, imageUrl, visible);
        return p;
    }

    public static ShopProduct of(Long id, Long shopId, Long companyId, ShopProductCategory category, String name,
                                 String description, long price, String imageUrl, boolean visible, int sortOrder) {
        ShopProduct p = new ShopProduct();
        p.id = id;
        p.shopId = shopId;
        p.companyId = companyId;
        p.category = category;
        p.name = name;
        p.description = description;
        p.price = price;
        p.imageUrl = imageUrl;
        p.visible = visible;
        p.sortOrder = sortOrder;
        return p;
    }

    public void update(ShopProductCategory category, String name, String description, long price,
                       String imageUrl, boolean visible) {
        if (category == null || price < 0 || price > MAX_PRICE) {
            throw new BusinessException(ShopErrorCode.INVALID_SETTINGS);
        }
        this.category = category;
        this.name = Shop.requireText(name, 100);
        this.description = Shop.optionalText(description, 2000);
        this.price = price;
        this.imageUrl = Shop.imageUrl(imageUrl);
        this.visible = visible;
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public Long getCompanyId() { return companyId; }
    public ShopProductCategory getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public boolean isVisible() { return visible; }
    public int getSortOrder() { return sortOrder; }
}
