package com.greenlink.core.application.company.service;

import com.greenlink.core.application.company.dto.CompanyInfo;
import com.greenlink.core.application.company.usecase.CompanyQueryUseCase;
import com.greenlink.core.domain.company.port.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CompanyQueryService implements CompanyQueryUseCase {

    private final CompanyRepository companyRepository;

    public CompanyQueryService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Optional<CompanyInfo> findByBusinessNo(String businessNo) {
        return companyRepository.findByBusinessNo(businessNo)
            .map(CompanyInfo::from);
    }
}
