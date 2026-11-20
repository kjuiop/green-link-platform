package com.greenlink.core.domain.shop.model;

public enum ShopProductCategory {
    FUNERAL("근조"),
    CELEBRATION("축하"),
    ORCHID("난"),
    FLOWER("꽃"),
    PLANT("관엽"),
    RICE_WREATH("쌀화환"),
    ETC("기타");

    private final String label;

    ShopProductCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
