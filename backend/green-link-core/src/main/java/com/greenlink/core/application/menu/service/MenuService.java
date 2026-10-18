package com.greenlink.core.application.menu.service;

import com.greenlink.core.application.menu.dto.CreateMenuCommand;
import com.greenlink.core.application.menu.dto.MenuResult;
import com.greenlink.core.application.menu.dto.UpdateMenuCommand;
import com.greenlink.core.application.menu.usecase.CreateMenuUseCase;
import com.greenlink.core.application.menu.usecase.DeleteMenuUseCase;
import com.greenlink.core.application.menu.usecase.GetAllMenusUseCase;
import com.greenlink.core.application.menu.usecase.GetMenusByRoleUseCase;
import com.greenlink.core.application.menu.usecase.UpdateMenuUseCase;
import com.greenlink.core.domain.member.model.Role;
import com.greenlink.core.domain.menu.model.Menu;
import com.greenlink.core.domain.menu.port.MenuRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.menu.MenuErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MenuService implements GetAllMenusUseCase, GetMenusByRoleUseCase,
        CreateMenuUseCase, UpdateMenuUseCase, DeleteMenuUseCase {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public List<MenuResult> getAllMenus() {
        List<Menu> all = menuRepository.findAll();
        return buildTree(all);
    }

    @Override
    public List<MenuResult> getMenusByRole(Role role) {
        List<Menu> all = menuRepository.findAll();
        List<Menu> filtered = all.stream()
            .filter(m -> m.isAccessibleBy(role))
            .toList();
        return buildTree(filtered);
    }

    @Override
    @Transactional
    public MenuResult createMenu(CreateMenuCommand cmd) {
        Menu menu = Menu.create(cmd.name(), cmd.url(), cmd.sortOrder(), cmd.parentId(), cmd.roles());
        Menu saved = menuRepository.save(menu);
        return MenuResult.from(saved);
    }

    @Override
    @Transactional
    public MenuResult updateMenu(Long id, UpdateMenuCommand cmd) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MenuErrorCode.MENU_NOT_FOUND));
        menu.updateInfo(cmd.name(), cmd.url(), cmd.sortOrder(), cmd.roles());
        Menu saved = menuRepository.save(menu);
        return MenuResult.from(saved);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MenuErrorCode.MENU_NOT_FOUND));
        if (menu.hasChildren()) {
            throw new BusinessException(MenuErrorCode.MENU_HAS_CHILDREN);
        }
        menuRepository.delete(id);
    }

    private List<MenuResult> buildTree(List<Menu> menus) {
        Map<Long, List<Menu>> byParent = menus.stream()
            .filter(m -> m.getParentId() != null)
            .collect(Collectors.groupingBy(Menu::getParentId));

        return menus.stream()
            .filter(m -> m.getParentId() == null)
            .map(m -> toResult(m, byParent))
            .sorted(Comparator.comparingInt(MenuResult::sortOrder))
            .toList();
    }

    private MenuResult toResult(Menu menu, Map<Long, List<Menu>> byParent) {
        List<MenuResult> childResults = byParent.getOrDefault(menu.getId(), List.of())
            .stream()
            .sorted(Comparator.comparingInt(Menu::getSortOrder))
            .map(child -> toResult(child, byParent))
            .toList();

        List<String> roleNames = menu.getRoles().stream()
            .map(Enum::name)
            .toList();

        return new MenuResult(
            menu.getId(),
            menu.getName(),
            menu.getUrl(),
            menu.getSortOrder(),
            menu.getParentId(),
            roleNames,
            childResults
        );
    }
}
