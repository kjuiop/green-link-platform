package com.greenlink.core.application.menu.usecase;

import com.greenlink.core.application.menu.dto.MenuResult;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;

import java.util.List;
import java.util.Set;

public interface GetMenusByRoleUseCase {
    List<MenuResult> getMenusByRole(AdminRole role, Set<CompanyType> capabilities);
}
