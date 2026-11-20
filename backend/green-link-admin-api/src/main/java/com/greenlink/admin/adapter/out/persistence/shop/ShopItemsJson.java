package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.core.domain.shop.model.ShopOrderItem;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

final class ShopItemsJson {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<ShopOrderItem>> TYPE = new TypeReference<>() {};

    private ShopItemsJson() {}

    static String write(List<ShopOrderItem> items) {
        return MAPPER.writeValueAsString(items == null ? List.of() : items);
    }

    static List<ShopOrderItem> read(String json) {
        if (json == null || json.isBlank()) return List.of();
        return MAPPER.readValue(json, TYPE);
    }
}
