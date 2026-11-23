package com.greenlink.shopcore.application.auth.dto;

public record VerifiedIdentity(Long adminId, String loginId, String name, String companyName, boolean superAdmin) {}
