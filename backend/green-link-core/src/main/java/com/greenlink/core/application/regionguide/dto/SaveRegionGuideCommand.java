package com.greenlink.core.application.regionguide.dto;

import com.greenlink.core.domain.regionguide.model.RegionGuideCategory;

public record SaveRegionGuideCommand(
        String sido,
        String sigungu,
        String placeName,
        RegionGuideCategory category,
        String content,
        String aliases,
        int sortOrder
) {}
