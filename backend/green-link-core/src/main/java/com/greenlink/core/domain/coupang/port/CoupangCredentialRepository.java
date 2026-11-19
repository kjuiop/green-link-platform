package com.greenlink.core.domain.coupang.port;

import com.greenlink.core.domain.coupang.model.CoupangCredential;

import java.util.List;
import java.util.Optional;

public interface CoupangCredentialRepository {

    Optional<CoupangCredential> findByCompanyId(Long companyId);

    List<Long> findEnabledCompanyIds();

    CoupangCredential save(CoupangCredential credential);
}
