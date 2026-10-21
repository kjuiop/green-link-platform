package com.greenlink.core.application.menu.usecase;

import com.greenlink.core.application.menu.dto.CreateMenuCommand;
import com.greenlink.core.application.menu.dto.MenuResult;

public interface CreateMenuUseCase {
    MenuResult createMenu(CreateMenuCommand cmd);
}
