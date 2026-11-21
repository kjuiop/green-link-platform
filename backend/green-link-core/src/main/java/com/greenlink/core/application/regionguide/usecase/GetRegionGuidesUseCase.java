package com.greenlink.core.application.regionguide.usecase;

import com.greenlink.core.application.regionguide.dto.RegionGuideResult;

import java.util.List;

public interface GetRegionGuidesUseCase {
    List<RegionGuideResult> getActiveGuides();
    List<RegionGuideResult> getGuidesBySido(String sido);
}
