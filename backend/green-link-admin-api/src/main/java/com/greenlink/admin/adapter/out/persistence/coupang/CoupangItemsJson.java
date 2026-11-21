package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

public final class CoupangItemsJson {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<CoupangOrderSheet.Item>> TYPE = new TypeReference<>() {};
    private static final TypeReference<List<CustomerOrder.Item>> CUSTOMER_ITEM_TYPE = new TypeReference<>() {};

    private CoupangItemsJson() {}

    static String write(List<CoupangOrderSheet.Item> items) {
        return MAPPER.writeValueAsString(items == null ? List.of() : items);
    }

    static List<CoupangOrderSheet.Item> read(String json) {
        if (json == null || json.isBlank()) return List.of();
        return MAPPER.readValue(json, TYPE);
    }

    public static String writeItems(List<CustomerOrder.Item> items) {
        return MAPPER.writeValueAsString(items == null ? List.of() : items);
    }

    public static List<CustomerOrder.Item> readItems(String json) {
        if (json == null || json.isBlank()) return List.of();
        return MAPPER.readValue(json, CUSTOMER_ITEM_TYPE);
    }
}
