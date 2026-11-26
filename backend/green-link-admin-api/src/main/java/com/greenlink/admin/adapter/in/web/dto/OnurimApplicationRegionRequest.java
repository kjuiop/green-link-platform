package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OnurimApplicationRegionRequest(
    @NotBlank @Size(max = 30) @Pattern(regexp = "[^,|]*") String sido,
    @Size(max = 30) @Pattern(regexp = "[^,|]*") String sigungu
) {}
