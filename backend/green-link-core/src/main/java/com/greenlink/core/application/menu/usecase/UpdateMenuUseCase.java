package com.greenlink.core.application.menu.usecase;

import com.greenlink.core.application.menu.dto.MenuResult;
import com.greenlink.core.application.menu.dto.UpdateMenuCommand;

public interface UpdateMenuUseCase {
    MenuResult updateMenu(Long id, UpdateMenuCommand cmd);
}
