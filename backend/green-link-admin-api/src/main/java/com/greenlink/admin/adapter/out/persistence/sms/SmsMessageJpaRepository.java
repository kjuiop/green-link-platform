package com.greenlink.admin.adapter.out.persistence.sms;

import com.greenlink.core.domain.sms.model.MessageChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmsMessageJpaRepository extends JpaRepository<SmsMessageJpaEntity, Long> {
    List<SmsMessageJpaEntity> findByOrderId(Long orderId);
    List<SmsMessageJpaEntity> findByOrderIdIn(List<Long> orderIds);
    List<SmsMessageJpaEntity> findByOrderIdAndChannel(Long orderId, MessageChannel channel);
}
