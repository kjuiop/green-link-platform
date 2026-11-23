package com.greenlink.shopcore.application.auth.port;

import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;

public interface IdentityVerifier {
    VerifiedIdentity verify(String loginId, String password);
    VerifiedIdentity getAccount(Long adminId);
}
