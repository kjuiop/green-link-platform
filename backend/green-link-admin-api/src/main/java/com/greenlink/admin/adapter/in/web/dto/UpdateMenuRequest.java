package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.menu.dto.UpdateMenuCommand;
import com.greenlink.core.domain.admin.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateMenuRequest(
    @NotBlank String name,
    @NotBlank String url,
    int sortOrder,
    @NotNull List<String> roles
) {
    public UpdateMenuCommand toCommand() {
        List<Role> roleList = roles.stream().map(Role::valueOf).toList();
        return new UpdateMenuCommand(name, url, sortOrder, roleList);
    }
}
