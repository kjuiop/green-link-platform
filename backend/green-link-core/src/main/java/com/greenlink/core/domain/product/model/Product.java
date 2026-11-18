package com.greenlink.core.domain.product.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;

import java.util.Objects;

public class Product {

    private Long id;
    private Long companyId;
    private Long baseProductId;
    private String name;
    private long price;
    private String memo;
    private String createdAt;
    private int sortOrder;
    private boolean enabled;

    private Product() {}

    public static Product createOwn(Long companyId, String name, long price, String memo, int sortOrder) {
        if (companyId == null) throw new IllegalArgumentException("수주사 상품은 소속 회사가 필요합니다.");
        requireNonNegative(price);
        Product p = new Product();
        p.companyId = companyId;
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.sortOrder = sortOrder;
        p.enabled = true;
        return p;
    }

    public static Product copyOf(Long companyId, BaseProduct base) {
        if (companyId == null) throw new IllegalArgumentException("수주사 상품은 소속 회사가 필요합니다.");
        Product p = new Product();
        p.companyId = companyId;
        p.baseProductId = base.getId();
        p.name = base.getName();
        p.price = base.getPrice();
        p.memo = base.getMemo();
        p.sortOrder = base.getSortOrder();
        p.enabled = true;
        return p;
    }

    public static Product of(Long id, Long companyId, Long baseProductId, String name, long price,
                             String memo, String createdAt, int sortOrder, boolean enabled) {
        Product p = new Product();
        p.id = id;
        p.companyId = companyId;
        p.baseProductId = baseProductId;
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.createdAt = createdAt;
        p.sortOrder = sortOrder;
        p.enabled = enabled;
        return p;
    }

    public boolean isBaseCopy() { return baseProductId != null; }

    public boolean isOwnedBy(Long companyId) { return Objects.equals(this.companyId, companyId); }

    public void update(String name, long price, String memo, int sortOrder) {
        requireOwn();
        requireNonNegative(price);
        this.name = name;
        this.price = price;
        this.memo = memo;
        this.sortOrder = sortOrder;
    }

    public void changeSetting(long price, boolean enabled) {
        requireNonNegative(price);
        this.price = price;
        this.enabled = enabled;
    }

    public void applyBaseUpdate(BaseProduct base) {
        this.name = base.getName();
        this.memo = base.getMemo();
        this.sortOrder = base.getSortOrder();
    }

    public void requireOwn() {
        if (isBaseCopy()) throw new BusinessException(ProductErrorCode.PRODUCT_BASE_COPY_READONLY);
    }

    private static void requireNonNegative(long price) {
        if (price < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public Long getBaseProductId() { return baseProductId; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getMemo() { return memo; }
    public String getCreatedAt() { return createdAt; }
    public int getSortOrder() { return sortOrder; }
    public boolean isEnabled() { return enabled; }
}
