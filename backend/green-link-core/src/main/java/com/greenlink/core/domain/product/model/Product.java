package com.greenlink.core.domain.product.model;

public class Product {

    private Long id;
    private String name;
    private long price;
    private String memo;
    private String createdAt;

    private Product() {}

    public static Product create(String name, long price, String memo) {
        Product p = new Product();
        p.name = name;
        p.price = price;
        p.memo = memo;
        return p;
    }

    public static Product of(Long id, String name, long price, String memo, String createdAt) {
        Product p = new Product();
        p.id = id;
        p.name = name;
        p.price = price;
        p.memo = memo;
        p.createdAt = createdAt;
        return p;
    }

    public void update(String name, long price, String memo) {
        this.name = name;
        this.price = price;
        this.memo = memo;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getMemo() { return memo; }
    public String getCreatedAt() { return createdAt; }
}
