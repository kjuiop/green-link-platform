package com.greenlink.core.application.company.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.CompanyInfo;
import com.greenlink.core.application.company.usecase.CompanyQueryUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
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

    @Override
    public List<CompanyInfo> findAllByType(CompanyType type) {
        return companyRepository.findAllByType(type).stream()
            .map(CompanyInfo::from)
            .toList();
    }

    @Override
    public PageResult<CompanyInfo> findAllPaged(int page, int size) {
        return PageResult.of(
            companyRepository.findAllDisplayed(PageRequest.of(page, size))
                .map(CompanyInfo::from)
        );
    }

    @Override
    @Transactional
    public void updateCompanyType(Long id, String type) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("업체를 찾을 수 없습니다: " + id));
        company.updateType(CompanyType.valueOf(type.toUpperCase()));
        companyRepository.save(company);
    }
}
