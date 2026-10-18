package com.greenlink.api.domain.company.controller;

import com.greenlink.api.domain.company.dto.CompanySearchResponse;
import com.greenlink.api.domain.company.repository.CompanyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<CompanySearchResponse> search(@RequestParam String businessNo) {
        return companyRepository.findByBusinessNo(businessNo)
            .map(CompanySearchResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
