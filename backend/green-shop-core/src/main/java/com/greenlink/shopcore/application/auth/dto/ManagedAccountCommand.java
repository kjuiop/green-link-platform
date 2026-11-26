package com.greenlink.shopcore.application.auth.dto;

public record ManagedAccountCommand(String loginId, String name, String phone, String email, Long companyId) {}
