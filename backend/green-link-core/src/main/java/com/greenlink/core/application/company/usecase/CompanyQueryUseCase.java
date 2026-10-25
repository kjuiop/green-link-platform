package com.greenlink.core.application.company.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.CompanyInfo;
import com.greenlink.core.domain.company.model.CompanyType;

import java.util.List;
import java.util.Optional;

public interface CompanyQueryUseCase {
    Optional<CompanyInfo> findByBusinessNo(String businessNo);
    List<CompanyInfo> findAllByType(CompanyType type);
    PageResult<CompanyInfo> findAllPaged(int page, int size);
    void updateCompanyType(Long id, String type);
}
