package com.greenlink.core.application.menu.dto;

import com.greenlink.core.domain.member.model.Role;

import java.util.List;

public record CreateMenuCommand(
    String name,
    String url,
    int sortOrder,
    Long parentId,
    List<Role> roles
) {}
