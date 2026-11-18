package com.greenlink.core.domain.product.model;

public class BaseProduct {

    private Long id;
    private String name;
    private long price;
    private String memo;
    private String createdAt;
    private int sortOrder;

    private BaseProduct() {}

    public static BaseProduct create(String name, long price, String memo, int sortOrder) {
        requireNonNegative(price);
        BaseProduct p = new BaseProduct();
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.sortOrder = sortOrder;
        return p;
    }

    public static BaseProduct of(Long id, String name, long price, String memo, String createdAt, int sortOrder) {
        BaseProduct p = new BaseProduct();
        p.id = id;
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.createdAt = createdAt;
        p.sortOrder = sortOrder;
        return p;
    }

    public void update(String name, long price, String memo, int sortOrder) {
        requireNonNegative(price);
        this.name = name;
        this.price = price;
        this.memo = memo;
        this.sortOrder = sortOrder;
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
}
