package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.ShopSignupCommand;
import com.greenlink.core.application.auth.dto.ShopSignupResult;

public interface ShopSignupUseCase {
    ShopSignupResult signup(ShopSignupCommand command);
    boolean existsBusinessNo(String businessNo);
}
