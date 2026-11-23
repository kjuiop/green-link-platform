package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.domain.menu.model.ShopMenu;

public record MenuResponse(Long id, String name, String url, String icon) {

    public static MenuResponse from(ShopMenu m) {
        return new MenuResponse(m.getId(), m.getName(), m.getUrl(), m.getIcon());
    }
}
