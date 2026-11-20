package com.greenlink.admin.adapter.out.persistence.fax;

import com.greenlink.core.domain.fax.model.FaxMessage;
import com.greenlink.core.domain.fax.model.FaxStatus;
import com.greenlink.core.domain.fax.port.FaxMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FaxMessageRepositoryAdapter implements FaxMessageRepository {

    private final FaxMessageJpaRepository jpaRepository;

    public FaxMessageRepositoryAdapter(FaxMessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<FaxMessage> findById(Long id) {
        return jpaRepository.findById(id).map(FaxMessageJpaEntity::toDomain);
    }

    @Override
    public Optional<FaxMessage> findLatestEffectiveByOrderId(Long orderId) {
        return jpaRepository.findFirstByOrderIdAndStatusNotOrderBySequenceDesc(orderId, FaxStatus.FAILED)
                .map(FaxMessageJpaEntity::toDomain);
    }

    @Override
    public int findMaxSequence(Long orderId) {
        return jpaRepository.findMaxSequence(orderId);
    }

    @Override
    public Optional<FaxMessage> saveIfAbsent(FaxMessage message) {
        if (jpaRepository.existsByOrderIdAndSequence(message.getOrderId(), message.getSequence())) {
            return Optional.empty();
        }
        return Optional.of(jpaRepository.saveAndFlush(FaxMessageJpaEntity.from(message)).toDomain());
    }

    @Override
    public FaxMessage save(FaxMessage message) {
        return jpaRepository.save(FaxMessageJpaEntity.from(message)).toDomain();
    }

    @Override
    public List<FaxMessage> findByStatus(FaxStatus status, int limit) {
        return jpaRepository.findByStatusOrderByIdAsc(status, PageRequest.of(0, limit)).stream()
                .map(FaxMessageJpaEntity::toDomain).toList();
    }

    @Override
    public Page<FaxMessage> search(FaxStatus status, String keyword, Pageable pageable) {
        String k = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return jpaRepository.search(status, k, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .map(FaxMessageJpaEntity::toDomain);
    }
}
