package com.greenlink.core.domain.product.model;

public class BaseProduct {

    private Long id;
    private String name;
    private long price;
    private String memo;
    private String createdAt;
    private int sortOrder;
    private ProductCategory category;

    private BaseProduct() {}

    public static BaseProduct create(String name, long price, String memo, int sortOrder, ProductCategory category) {
        requireNonNegative(price);
        requireCategory(category);
        BaseProduct p = new BaseProduct();
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.sortOrder = sortOrder;
        p.category = category;
        return p;
    }

    public static BaseProduct of(Long id, String name, long price, String memo, String createdAt, int sortOrder,
                                 ProductCategory category) {
        BaseProduct p = new BaseProduct();
        p.id = id;
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.createdAt = createdAt;
        p.sortOrder = sortOrder;
        p.category = category;
        return p;
    }

    public void update(String name, long price, String memo, int sortOrder, ProductCategory category) {
        requireNonNegative(price);
        requireCategory(category);
        this.name = name;
        this.price = price;
        this.memo = memo;
        this.sortOrder = sortOrder;
        this.category = category;
    }

    private static void requireCategory(ProductCategory category) {
        if (category == null) throw new IllegalArgumentException("상품 종류는 필수입니다.");
    }

    private static void requireNonNegative(long price) {
        if (price < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getMemo() { return memo; }
    public String getCreatedAt() { return createdAt; }
    public int getSortOrder() { return sortOrder; }
    public ProductCategory getCategory() { return category; }
}
