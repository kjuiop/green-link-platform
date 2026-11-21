package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.regionguide.dto.RegionGuideResult;
import com.greenlink.core.application.regionguide.usecase.GetRegionGuidesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/region-guides")
public class RegionGuideController {

    private final GetRegionGuidesUseCase getRegionGuidesUseCase;

    public RegionGuideController(GetRegionGuidesUseCase getRegionGuidesUseCase) {
        this.getRegionGuidesUseCase = getRegionGuidesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RegionGuideResult>> list() {
        return ResponseEntity.ok(getRegionGuidesUseCase.getActiveGuides());
    }
}
