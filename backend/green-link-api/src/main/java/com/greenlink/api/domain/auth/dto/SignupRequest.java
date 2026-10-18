package com.greenlink.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SignupRequest(
    @NotBlank @Size(min = 6, max = 50)
    String loginId,

    @NotBlank @Size(min = 8, max = 100)
    String password,

    @NotBlank @Size(max = 50)
    String name,

    @NotBlank @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$")
    String phone,

    @NotBlank @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$")
    String businessNo,

    @NotBlank @Size(max = 100)
    String companyName,

    @Size(max = 50)
    String ceoName,

    @Size(max = 20)
    String companyPhone,

    @Size(max = 200)
    String address,

    @NotEmpty
    List<String> roles
) {}
