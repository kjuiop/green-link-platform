package com.greenlink.core.application.menu.dto;

import com.greenlink.core.domain.menu.model.Menu;

import java.util.List;

public record MenuResult(
    Long id,
    String name,
    String url,
    int sortOrder,
    Long parentId,
    List<String> roles,
    List<MenuResult> children
) {
    public static MenuResult from(Menu menu) {
        List<String> roleNames = menu.getRoles().stream()
            .map(Enum::name)
            .toList();
        List<MenuResult> childResults = menu.getChildren().stream()
            .map(MenuResult::from)
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
