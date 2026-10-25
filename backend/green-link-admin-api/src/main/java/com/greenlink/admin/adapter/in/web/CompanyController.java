package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.CompanyInfo;
import com.greenlink.core.application.company.usecase.CompanyQueryUseCase;
import com.greenlink.core.domain.company.model.CompanyType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyQueryUseCase companyQueryUseCase;

    public CompanyController(CompanyQueryUseCase companyQueryUseCase) {
        this.companyQueryUseCase = companyQueryUseCase;
    }

    @GetMapping("/search")
    public ResponseEntity<CompanyInfo> search(@RequestParam String businessNo) {
        return companyQueryUseCase.findByBusinessNo(businessNo)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CompanyInfo>> list(@RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            try {
                CompanyType companyType = CompanyType.valueOf(type.toUpperCase());
                return ResponseEntity.ok(companyQueryUseCase.findAllByType(companyType));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(companyQueryUseCase.findAllByType(CompanyType.ORDERING));
    }

    @GetMapping("/paged")
    public ResponseEntity<PageResult<CompanyInfo>> paged(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(companyQueryUseCase.findAllPaged(page, size));
    }

    @PutMapping("/{id}/type")
    public ResponseEntity<Void> updateType(
        @PathVariable Long id,
        @RequestBody Map<String, String> body
    ) {
        String type = body.get("type");
        if (type == null || type.isBlank()) return ResponseEntity.badRequest().build();
        try {
            companyQueryUseCase.updateCompanyType(id, type);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
