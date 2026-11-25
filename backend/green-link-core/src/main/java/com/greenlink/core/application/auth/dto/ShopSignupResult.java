package com.greenlink.core.application.auth.dto;

public record ShopSignupResult(
    VerifiedAccount account,
    Long targetCompanyId,
    boolean founder
) {}
