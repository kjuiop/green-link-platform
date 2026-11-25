package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.AccountSummary;
import com.greenlink.core.application.auth.dto.VerifiedAccount;

import java.util.Collection;
import java.util.List;

public interface VerifyAdminAccountUseCase {
    VerifiedAccount verify(String loginId, String rawPassword);
    VerifiedAccount getActiveAccount(Long adminId);
    List<AccountSummary> getAccountSummaries(Collection<Long> adminIds);
}
