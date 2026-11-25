package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.auth.dto.SignupResult;

public record SignupResponse(boolean founder) {
    public static SignupResponse from(SignupResult result) {
        return new SignupResponse(result.founder());
    }
}
