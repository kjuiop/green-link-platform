package com.greenlink.core.domain.company.port;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyCapability;
import com.greenlink.core.domain.company.model.CompanyRegion;
import com.greenlink.core.domain.company.model.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository {
    Optional<Company> findByBusinessNo(String businessNo);
    Company save(Company company);
    Optional<Company> findById(Long id);
    boolean existsByBusinessNo(String businessNo);
    List<Company> findAllByIdIn(List<Long> ids);
    List<Company> findAllByType(CompanyType type);
    List<Company> findAllByTypeIn(List<CompanyType> types);
    Optional<Company> findByCompanyName(String companyName);
    Page<Company> findAllDisplayed(Pageable pageable);

    void saveRegions(Long companyId, List<CompanyRegion> regions);
    void saveCapabilities(Long companyId, List<CompanyCapability> capabilities);
    List<CompanyRegion> findRegionsByCompanyId(Long companyId);
    List<CompanyCapability> findCapabilitiesByCompanyId(Long companyId);
}
