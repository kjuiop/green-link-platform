package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.company.dto.CompanyInfo;
import com.greenlink.core.application.company.usecase.CompanyQueryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
