package com.greenlink.api.domain.company.service;

import com.greenlink.api.domain.company.dto.CompanySearchResponse;
import com.greenlink.api.domain.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Optional<CompanySearchResponse> findByBusinessNo(String businessNo) {
        return companyRepository.findByBusinessNo(businessNo)
            .map(CompanySearchResponse::from);
    }
}
