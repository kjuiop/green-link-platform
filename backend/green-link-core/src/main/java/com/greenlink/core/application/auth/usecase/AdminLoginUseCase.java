package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.AdminLoginCommand;
import com.greenlink.core.application.auth.dto.AdminLoginResult;

public interface AdminLoginUseCase {
    AdminLoginResult login(AdminLoginCommand cmd);
}
