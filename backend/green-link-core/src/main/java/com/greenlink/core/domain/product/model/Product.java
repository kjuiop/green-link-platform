package com.greenlink.core.domain.product.model;

public class Product {

    private Long id;
    private String name;
    private long price;
    private String memo;
    private String createdAt;
    private int sortOrder;

    private Product() {}

    public static Product create(String name, long price, String memo, int sortOrder) {
        Product p = new Product();
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.sortOrder = sortOrder;
        return p;
    }

    public static Product of(Long id, String name, long price, String memo, String createdAt, int sortOrder) {
        Product p = new Product();
        p.id = id;
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.createdAt = createdAt;
        p.sortOrder = sortOrder;
        return p;
    }

    public void update(String name, long price, String memo, int sortOrder) {
        this.name = name;
        this.price = price;
        this.memo = memo;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getMemo() { return memo; }
    public String getCreatedAt() { return createdAt; }
    public int getSortOrder() { return sortOrder; }
}
