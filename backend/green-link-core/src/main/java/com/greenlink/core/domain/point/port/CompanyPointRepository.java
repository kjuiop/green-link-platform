package com.greenlink.core.domain.point.port;

import com.greenlink.core.domain.point.model.CompanyPoint;

import java.util.List;
import java.util.Optional;

public interface CompanyPointRepository {
    Optional<CompanyPoint> findByCompanyId(Long companyId);
    Optional<CompanyPoint> findByCompanyIdForUpdate(Long companyId);
    List<CompanyPoint> findAllByCompanyIdIn(List<Long> companyIds);
    CompanyPoint save(CompanyPoint point);
}
