package com.greenlink.shopcore.application.menu.usecase;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.menu.model.ShopMenu;

import java.util.List;

public interface GetMenusUseCase {
    List<ShopMenu> getMenus(ShopRole role);
}
