package com.greenlink.shopcore.domain.brand.model;

public class ShopBrandMember {

    private Long id;
    private Long brandId;
    private Long memberId;

    private ShopBrandMember() {}

    public static ShopBrandMember assign(Long brandId, Long memberId) {
        ShopBrandMember m = new ShopBrandMember();
        m.brandId = brandId;
        m.memberId = memberId;
        return m;
    }

    public static ShopBrandMember of(Long id, Long brandId, Long memberId) {
        ShopBrandMember m = assign(brandId, memberId);
        m.id = id;
        return m;
    }

    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public Long getMemberId() { return memberId; }
}
