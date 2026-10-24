package com.greenlink.core.application.auth.usecase;

public interface VerifyPhoneCodeUseCase {
    void verify(String phone, String code);
}
