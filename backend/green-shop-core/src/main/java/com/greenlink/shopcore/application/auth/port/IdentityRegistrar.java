package com.greenlink.shopcore.application.auth.port;

import com.greenlink.shopcore.application.auth.dto.ManagedAccountCommand;
import com.greenlink.shopcore.application.auth.dto.RegisteredAccount;
import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;

public interface IdentityRegistrar {
    RegisteredAccount register(SignupCommand command);
    boolean businessNoExists(String businessNo);
    VerifiedIdentity createManagedAccount(ManagedAccountCommand command);
    void completePasswordSetup(String loginId, String phone, String newPassword);
    void reissuePasswordSetup(Long adminId);
}
