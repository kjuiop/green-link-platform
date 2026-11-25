package com.greenlink.shopcore.application.auth.port;

import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IdentityVerifier {
    VerifiedIdentity verify(String loginId, String password);
    VerifiedIdentity getAccount(Long adminId);
    List<AccountProfile> getAccounts(Collection<Long> adminIds);
    Map<Long, String> getCompanyNames(Collection<Long> companyIds);
}
