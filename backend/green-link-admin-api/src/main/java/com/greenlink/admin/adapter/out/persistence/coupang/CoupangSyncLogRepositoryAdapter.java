package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.core.domain.coupang.model.CoupangSyncLog;
import com.greenlink.core.domain.coupang.port.CoupangSyncLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoupangSyncLogRepositoryAdapter implements CoupangSyncLogRepository {

    private final CoupangSyncLogJpaRepository jpaRepository;

    public CoupangSyncLogRepositoryAdapter(CoupangSyncLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CoupangSyncLog save(CoupangSyncLog log) {
        return jpaRepository.save(CoupangSyncLogJpaEntity.from(log)).toDomain();
    }

    @Override
    public List<CoupangSyncLog> findRecent(Long companyId, int limit) {
        return jpaRepository.findByCompanyIdOrderByIdDesc(companyId, PageRequest.of(0, limit)).stream()
                .map(CoupangSyncLogJpaEntity::toDomain).toList();
    }
}
