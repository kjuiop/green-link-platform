package com.greenlink.shopcore.application.auth.dto;

public record SignupCommand(String loginId, String password, String name, String phone, String email,
                            String businessNo, String companyName, String ceoName, String requestedRole) {}
