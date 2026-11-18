package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.Min;

public record ProductSettingRequest(
        @Min(0) long price,
        boolean enabled
) {}
