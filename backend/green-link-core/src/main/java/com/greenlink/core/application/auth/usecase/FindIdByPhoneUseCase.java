package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.FindIdResult;

public interface FindIdByPhoneUseCase {
    FindIdResult findId(String phone);
}
