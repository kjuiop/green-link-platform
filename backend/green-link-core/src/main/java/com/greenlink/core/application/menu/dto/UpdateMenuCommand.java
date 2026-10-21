package com.greenlink.core.application.menu.dto;

import com.greenlink.core.domain.admin.model.Role;

import java.util.List;

public record UpdateMenuCommand(
    String name,
    String url,
    int sortOrder,
    List<Role> roles
) {}
