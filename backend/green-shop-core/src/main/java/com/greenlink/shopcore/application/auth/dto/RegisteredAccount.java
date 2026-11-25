package com.greenlink.shopcore.application.auth.dto;

public record RegisteredAccount(VerifiedIdentity identity, Long targetCompanyId, boolean founder) {}
