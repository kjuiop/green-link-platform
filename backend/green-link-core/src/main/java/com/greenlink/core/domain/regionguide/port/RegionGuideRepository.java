package com.greenlink.core.domain.regionguide.port;

import com.greenlink.core.domain.regionguide.model.RegionGuide;

import java.util.List;
import java.util.Optional;

public interface RegionGuideRepository {
    RegionGuide save(RegionGuide guide);
    Optional<RegionGuide> findById(Long id);
    List<RegionGuide> findAllActive();
    List<RegionGuide> findBySido(String sido);
    void deleteById(Long id);
}
