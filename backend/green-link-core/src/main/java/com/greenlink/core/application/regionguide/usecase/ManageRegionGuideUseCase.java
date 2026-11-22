package com.greenlink.core.application.regionguide.usecase;

import com.greenlink.core.application.regionguide.dto.RegionGuideResult;
import com.greenlink.core.application.regionguide.dto.SaveRegionGuideCommand;

public interface ManageRegionGuideUseCase {
    RegionGuideResult create(SaveRegionGuideCommand command, Long updatedBy);
    RegionGuideResult update(Long id, SaveRegionGuideCommand command, Long updatedBy);
    RegionGuideResult changeActive(Long id, boolean active, Long updatedBy);
    void delete(Long id);
}
