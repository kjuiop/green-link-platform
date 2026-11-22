package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.SaveRegionGuideRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateRegionGuideActiveRequest;
import com.greenlink.core.application.regionguide.dto.RegionGuideResult;
import com.greenlink.core.application.regionguide.usecase.GetRegionGuidesUseCase;
import com.greenlink.core.application.regionguide.usecase.ManageRegionGuideUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/region-guides")
public class AdminRegionGuideController {

    private final GetRegionGuidesUseCase getRegionGuidesUseCase;
    private final ManageRegionGuideUseCase manageRegionGuideUseCase;

    public AdminRegionGuideController(GetRegionGuidesUseCase getRegionGuidesUseCase,
                                      ManageRegionGuideUseCase manageRegionGuideUseCase) {
        this.getRegionGuidesUseCase = getRegionGuidesUseCase;
        this.manageRegionGuideUseCase = manageRegionGuideUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RegionGuideResult>> list(@RequestParam String sido) {
        return ResponseEntity.ok(getRegionGuidesUseCase.getGuidesBySido(sido));
    }

    @PostMapping
    public ResponseEntity<RegionGuideResult> create(@RequestBody @Valid SaveRegionGuideRequest req,
                                                    Authentication authentication) {
        return ResponseEntity.ok(manageRegionGuideUseCase.create(req.toCommand(), memberId(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionGuideResult> update(@PathVariable Long id,
                                                    @RequestBody @Valid SaveRegionGuideRequest req,
                                                    Authentication authentication) {
        return ResponseEntity.ok(manageRegionGuideUseCase.update(id, req.toCommand(), memberId(authentication)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<RegionGuideResult> changeActive(@PathVariable Long id,
                                                          @RequestBody UpdateRegionGuideActiveRequest req,
                                                          Authentication authentication) {
        return ResponseEntity.ok(manageRegionGuideUseCase.changeActive(id, req.active(), memberId(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        manageRegionGuideUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Long memberId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
