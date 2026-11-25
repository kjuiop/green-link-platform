package com.greenlink.shopcore.domain.brand.model;

public class ShopBrand {

    private Long id;
    private Long companyId;
    private String name;

    private ShopBrand() {}

    public static ShopBrand create(Long companyId, String name) {
        ShopBrand b = new ShopBrand();
        b.companyId = companyId;
        b.name = name;
        return b;
    }

    public static ShopBrand of(Long id, Long companyId, String name) {
        ShopBrand b = create(companyId, name);
        b.id = id;
        return b;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getName() { return name; }
}
