package com.greenlink.core.application.regionguide.service;

import com.greenlink.core.application.regionguide.dto.RegionGuideResult;
import com.greenlink.core.application.regionguide.dto.SaveRegionGuideCommand;
import com.greenlink.core.application.regionguide.usecase.GetRegionGuidesUseCase;
import com.greenlink.core.application.regionguide.usecase.ManageRegionGuideUseCase;
import com.greenlink.core.domain.regionguide.model.RegionGuide;
import com.greenlink.core.domain.regionguide.port.RegionGuideRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.regionguide.RegionGuideErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RegionGuideService implements GetRegionGuidesUseCase, ManageRegionGuideUseCase {

    private final RegionGuideRepository regionGuideRepository;

    public RegionGuideService(RegionGuideRepository regionGuideRepository) {
        this.regionGuideRepository = regionGuideRepository;
    }

    @Override
    public List<RegionGuideResult> getActiveGuides() {
        return regionGuideRepository.findAllActive().stream().map(RegionGuideResult::from).toList();
    }

    @Override
    public List<RegionGuideResult> getGuidesBySido(String sido) {
        return regionGuideRepository.findBySido(sido).stream().map(RegionGuideResult::from).toList();
    }

    @Override
    @Transactional
    public RegionGuideResult create(SaveRegionGuideCommand c, Long updatedBy) {
        RegionGuide guide = RegionGuide.create(c.sido(), c.sigungu(), c.placeName(), c.category(),
                c.content(), c.aliases(), c.sortOrder(), updatedBy);
        return RegionGuideResult.from(regionGuideRepository.save(guide));
    }

    @Override
    @Transactional
    public RegionGuideResult update(Long id, SaveRegionGuideCommand c, Long updatedBy) {
        RegionGuide guide = find(id);
        guide.update(c.sido(), c.sigungu(), c.placeName(), c.category(),
                c.content(), c.aliases(), c.sortOrder(), updatedBy);
        return RegionGuideResult.from(regionGuideRepository.save(guide));
    }

    @Override
    @Transactional
    public RegionGuideResult changeActive(Long id, boolean active, Long updatedBy) {
        RegionGuide guide = find(id);
        guide.changeActive(active, updatedBy);
        return RegionGuideResult.from(regionGuideRepository.save(guide));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        find(id);
        regionGuideRepository.deleteById(id);
    }

    private RegionGuide find(Long id) {
        return regionGuideRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RegionGuideErrorCode.REGION_GUIDE_NOT_FOUND));
    }
}
