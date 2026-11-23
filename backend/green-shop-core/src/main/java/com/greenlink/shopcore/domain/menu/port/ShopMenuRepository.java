package com.greenlink.shopcore.domain.menu.port;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.menu.model.ShopMenu;

import java.util.List;

public interface ShopMenuRepository {
    List<ShopMenu> findByRole(ShopRole role);
    long count();
    ShopMenu save(ShopMenu menu);
}
