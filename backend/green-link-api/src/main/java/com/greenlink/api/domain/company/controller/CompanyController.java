package com.greenlink.api.domain.company.controller;

import com.greenlink.api.domain.company.dto.CompanySearchResponse;
import com.greenlink.api.domain.company.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/search")
    public ResponseEntity<CompanySearchResponse> search(@RequestParam String businessNo) {
        return companyService.findByBusinessNo(businessNo)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
