package com.greenlink.core.application.auth.dto;

import java.util.List;

public record SignupCommand(
    String loginId,
    String password,
    String name,
    String phone,
    String businessNo,
    String companyName,
    String ceoName,
    String companyPhone,
    String address,
    List<String> roles
) {}
