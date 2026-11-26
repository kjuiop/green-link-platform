package com.greenlink.shopcore.application.auth.usecase;

public interface ShopPasswordSetupUseCase {
    void setup(String loginId, String phone, String newPassword, String clientKey);
}
