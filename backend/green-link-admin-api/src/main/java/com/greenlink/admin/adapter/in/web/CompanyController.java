package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.company.dto.CompanyInfo;
import com.greenlink.core.application.company.usecase.CompanyQueryUseCase;
import com.greenlink.core.domain.company.model.CompanyType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
