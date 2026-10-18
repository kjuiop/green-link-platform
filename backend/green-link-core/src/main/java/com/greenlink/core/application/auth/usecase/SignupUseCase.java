package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.SignupCommand;

public interface SignupUseCase {
    void signup(SignupCommand command);
}
