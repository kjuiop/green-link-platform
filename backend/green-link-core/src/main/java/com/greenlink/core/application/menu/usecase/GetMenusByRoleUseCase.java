package com.greenlink.core.application.menu.usecase;

import com.greenlink.core.application.menu.dto.MenuResult;
import com.greenlink.core.domain.admin.model.Role;

import java.util.List;

public interface GetMenusByRoleUseCase {
    List<MenuResult> getMenusByRole(Role role);
}
