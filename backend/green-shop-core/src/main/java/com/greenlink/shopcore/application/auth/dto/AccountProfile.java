package com.greenlink.shopcore.application.auth.dto;

public record AccountProfile(Long adminId, String loginId, String name, String companyName,
                             boolean passwordSetupPending, boolean onurimEnabled,
                             boolean companyOnurimEnabled) {}
