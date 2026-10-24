package com.greenlink.core.application.auth.usecase;

public interface ResetPasswordUseCase {
    void reset(String phone, String loginId, String newPassword);
}
