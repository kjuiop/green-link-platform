package com.greenlink.core.application.point.usecase;

import com.greenlink.core.application.point.dto.CompanyPointResult;

import java.util.List;

public interface GetAdminPointListUseCase {
    List<CompanyPointResult> getAdminPointList();
}
