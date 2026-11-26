package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnurimApplicationRegionRequest(
    @NotBlank @Size(max = 30) String sido,
    @Size(max = 30) String sigungu
) {}
