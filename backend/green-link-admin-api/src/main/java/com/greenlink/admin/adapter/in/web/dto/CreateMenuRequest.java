package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.menu.dto.CreateMenuCommand;
import com.greenlink.core.domain.admin.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateMenuRequest(
    @NotBlank String name,
    @NotBlank String url,
    int sortOrder,
    Long parentId,
    @NotNull List<String> roles
) {
    public CreateMenuCommand toCommand() {
        List<Role> roleList = roles.stream().map(Role::valueOf).toList();
        return new CreateMenuCommand(name, url, sortOrder, parentId, roleList);
    }
}
