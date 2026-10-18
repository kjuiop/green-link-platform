package com.greenlink.core.application.menu.usecase;

import com.greenlink.core.application.menu.dto.MenuResult;

import java.util.List;

public interface GetAllMenusUseCase {
    List<MenuResult> getAllMenus();
}
