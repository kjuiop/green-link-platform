package com.greenlink.admin.adapter.out.persistence.sms;

import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SmsMessageRepositoryAdapter implements SmsMessageRepository {

    private final SmsMessageJpaRepository jpaRepository;

    public SmsMessageRepositoryAdapter(SmsMessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SmsMessage save(SmsMessage message) {
        return jpaRepository.save(SmsMessageJpaEntity.from(message)).toDomain();
    }

    @Override
    public List<SmsMessage> findAllByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).stream()
                .map(SmsMessageJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<SmsMessage> findAll() {
        return jpaRepository.findAll().stream()
                .map(SmsMessageJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Page<SmsMessage> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(SmsMessageJpaEntity::toDomain);
    }
}
