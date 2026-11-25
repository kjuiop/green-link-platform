package com.greenlink.shopcore.application.auth.usecase;

import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import com.greenlink.shopcore.application.auth.dto.SignupResult;

public interface ShopSignupUseCase {
    SignupResult signup(SignupCommand command, String clientKey);
    boolean businessNoExists(String businessNo, String clientKey);
}
