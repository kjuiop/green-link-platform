package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record FindIdRequest(@NotBlank String phone) {}
