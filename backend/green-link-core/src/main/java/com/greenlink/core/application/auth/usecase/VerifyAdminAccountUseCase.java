package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.VerifiedAccount;

public interface VerifyAdminAccountUseCase {
    VerifiedAccount verify(String loginId, String rawPassword);
    VerifiedAccount getActiveAccount(Long adminId);
}
