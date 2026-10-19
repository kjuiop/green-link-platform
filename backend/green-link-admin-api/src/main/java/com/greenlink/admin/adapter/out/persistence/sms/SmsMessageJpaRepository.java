package com.greenlink.admin.adapter.out.persistence.sms;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmsMessageJpaRepository extends JpaRepository<SmsMessageJpaEntity, Long> {
    List<SmsMessageJpaEntity> findByOrderId(Long orderId);
}
