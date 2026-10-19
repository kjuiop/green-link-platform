package com.greenlink.core.application.admin.dto;

public record UpdateAdminProfileCommand(Long adminId, String name, String phone) {}
