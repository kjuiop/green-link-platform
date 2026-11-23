package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.MenuResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.menu.usecase.GetMenusUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MenuController {

    private final GetMenusUseCase getMenusUseCase;

    public MenuController(GetMenusUseCase getMenusUseCase) {
        this.getMenusUseCase = getMenusUseCase;
    }

    @GetMapping("/api/menus")
    public List<MenuResponse> menus(@AuthenticationPrincipal ShopPrincipal principal) {
        return getMenusUseCase.getMenus(principal.role()).stream().map(MenuResponse::from).toList();
    }
}
