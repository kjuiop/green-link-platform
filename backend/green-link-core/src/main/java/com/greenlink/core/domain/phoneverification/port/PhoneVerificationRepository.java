package com.greenlink.core.domain.phoneverification.port;

import com.greenlink.core.domain.phoneverification.model.PhoneVerification;

import java.util.Optional;

public interface PhoneVerificationRepository {
    PhoneVerification save(PhoneVerification verification);
    Optional<PhoneVerification> findLatestByPhone(String phone);
}
