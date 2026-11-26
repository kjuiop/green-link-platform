package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.ShopManagedAccountCommand;
import com.greenlink.core.application.auth.dto.VerifiedAccount;

public interface ShopManagedAccountUseCase {
    VerifiedAccount create(ShopManagedAccountCommand command);
    void completePasswordSetup(String loginId, String phone, String newPassword);
    void reissuePasswordSetup(Long adminId);
}
