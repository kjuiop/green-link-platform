package com.greenlink.admin.adapter.out.persistence.phoneverification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationJpaRepository extends JpaRepository<PhoneVerificationJpaEntity, Long> {
    Optional<PhoneVerificationJpaEntity> findTopByPhoneOrderByCreatedAtDesc(String phone);
}
