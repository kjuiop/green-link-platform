package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.menu.dto.CreateMenuCommand;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateMenuRequest(
    @NotBlank String name,
    @NotBlank String url,
    int sortOrder,
    Long parentId,
    @NotNull List<String> roles,
    List<String> companyTypes
) {
    public CreateMenuCommand toCommand() {
        List<AdminRole> roleList = roles.stream().map(AdminRole::valueOf).toList();
        List<CompanyType> typeList = companyTypes == null ? List.of()
            : companyTypes.stream().map(CompanyType::valueOf).toList();
        return new CreateMenuCommand(name, url, sortOrder, parentId, roleList, typeList);
    }
}
