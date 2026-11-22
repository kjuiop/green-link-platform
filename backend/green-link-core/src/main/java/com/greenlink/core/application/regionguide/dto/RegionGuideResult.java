package com.greenlink.core.application.regionguide.dto;

import com.greenlink.core.domain.regionguide.model.RegionGuide;

public record RegionGuideResult(
        Long id,
        String sido,
        String sigungu,
        String placeName,
        String category,
        String content,
        String aliases,
        int sortOrder,
        boolean active,
        Long updatedBy,
        String updatedAt
) {
    public static RegionGuideResult from(RegionGuide g) {
        return new RegionGuideResult(g.getId(), g.getSido(), g.getSigungu(), g.getPlaceName(),
                g.getCategory().name(), g.getContent(), g.getAliases(), g.getSortOrder(),
                g.isActive(), g.getUpdatedBy(), g.getUpdatedAt());
    }
}
