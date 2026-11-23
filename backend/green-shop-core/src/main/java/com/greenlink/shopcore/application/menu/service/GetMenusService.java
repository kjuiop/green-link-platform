package com.greenlink.shopcore.application.menu.service;

import com.greenlink.shopcore.application.menu.usecase.GetMenusUseCase;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.menu.model.ShopMenu;
import com.greenlink.shopcore.domain.menu.port.ShopMenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetMenusService implements GetMenusUseCase {

    private final ShopMenuRepository menuRepository;

    public GetMenusService(ShopMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public List<ShopMenu> getMenus(ShopRole role) {
        return menuRepository.findByRole(role).stream()
            .sorted(Comparator.comparingInt(ShopMenu::getSortOrder))
            .toList();
    }
}
