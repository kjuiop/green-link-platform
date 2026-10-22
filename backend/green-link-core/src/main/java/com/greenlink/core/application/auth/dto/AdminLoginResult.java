package com.greenlink.core.application.auth.dto;

import com.greenlink.core.application.menu.dto.MenuResult;

import java.util.List;

public record AdminLoginResult(String accessToken, String name, List<MenuResult> menus) {}
