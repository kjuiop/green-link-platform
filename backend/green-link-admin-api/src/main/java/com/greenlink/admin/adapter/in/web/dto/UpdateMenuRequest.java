package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.menu.dto.UpdateMenuCommand;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateMenuRequest(
    @NotBlank String name,
    @NotBlank String url,
    int sortOrder,
    @NotNull List<String> roles,
    List<String> companyTypes
) {
    public UpdateMenuCommand toCommand() {
        List<AdminRole> roleList = roles.stream().map(AdminRole::valueOf).toList();
        List<CompanyType> typeList = companyTypes == null ? List.of()
            : companyTypes.stream().map(CompanyType::valueOf).toList();
        return new UpdateMenuCommand(name, url, sortOrder, roleList, typeList);
    }
}
