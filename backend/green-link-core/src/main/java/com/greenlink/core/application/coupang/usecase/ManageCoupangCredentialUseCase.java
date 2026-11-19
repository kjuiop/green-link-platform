package com.greenlink.core.application.coupang.usecase;

import com.greenlink.core.application.coupang.dto.CoupangCredentialResult;
import com.greenlink.core.application.coupang.dto.CoupangSyncLogResult;
import com.greenlink.core.application.coupang.dto.CoupangTestResult;
import com.greenlink.core.application.coupang.dto.SaveCoupangCredentialCommand;

import java.util.List;
import java.util.Optional;

public interface ManageCoupangCredentialUseCase {

    Optional<CoupangCredentialResult> get(Long companyId);

    CoupangCredentialResult save(SaveCoupangCredentialCommand cmd);

    CoupangTestResult test(Long companyId);

    List<CoupangSyncLogResult> recentSyncLogs(Long companyId, int limit);
}
