package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.regionguide.dto.SaveRegionGuideCommand;
import com.greenlink.core.domain.regionguide.model.RegionGuideCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveRegionGuideRequest(
        @NotBlank @Size(max = 20) String sido,
        @Size(max = 30) String sigungu,
        @Size(max = 100) String placeName,
        @NotNull RegionGuideCategory category,
        @NotBlank @Size(max = 1000) String content,
        @Size(max = 200) String aliases,
        @Min(0) @Max(100000) int sortOrder
) {
    public SaveRegionGuideCommand toCommand() {
        return new SaveRegionGuideCommand(sido, sigungu, placeName, category, content, aliases, sortOrder);
    }
}
