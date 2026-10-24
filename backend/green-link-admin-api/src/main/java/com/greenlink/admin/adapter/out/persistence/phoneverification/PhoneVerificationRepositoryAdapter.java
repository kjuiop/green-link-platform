package com.greenlink.admin.adapter.out.persistence.phoneverification;

import com.greenlink.core.domain.phoneverification.model.PhoneVerification;
import com.greenlink.core.domain.phoneverification.port.PhoneVerificationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PhoneVerificationRepositoryAdapter implements PhoneVerificationRepository {

    private final PhoneVerificationJpaRepository jpaRepository;

    public PhoneVerificationRepositoryAdapter(PhoneVerificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PhoneVerification save(PhoneVerification verification) {
        return jpaRepository.save(PhoneVerificationJpaEntity.from(verification)).toDomain();
    }

    @Override
    public Optional<PhoneVerification> findLatestByPhone(String phone) {
        return jpaRepository.findTopByPhoneOrderByCreatedAtDesc(phone)
                .map(PhoneVerificationJpaEntity::toDomain);
    }
}
