package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.CompanySummary;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.domain.admin.model.AdminRole;

import java.util.Collection;
import java.util.List;

public interface ShopAccessUseCase {
    VerifiedAccount grant(Long adminId, Long companyId, AdminRole role);
    void changeRole(Long adminId, AdminRole role);
    List<CompanySummary> getCompanySummaries(Collection<Long> companyIds);
}
