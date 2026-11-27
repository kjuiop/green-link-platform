package com.greenlink.core.application.menu.dto;

import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;

import java.util.List;

public record UpdateMenuCommand(
    String name,
    String url,
    int sortOrder,
    List<AdminRole> roles,
    List<CompanyType> companyTypes
) {}
