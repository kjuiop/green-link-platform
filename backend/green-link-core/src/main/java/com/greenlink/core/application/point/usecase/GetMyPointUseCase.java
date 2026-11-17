package com.greenlink.core.application.point.usecase;

import com.greenlink.core.application.point.dto.CompanyPointResult;

public interface GetMyPointUseCase {
    CompanyPointResult getMyPoint(Long companyId);
}
