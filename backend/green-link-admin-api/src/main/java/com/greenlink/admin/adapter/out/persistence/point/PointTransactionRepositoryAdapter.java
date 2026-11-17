package com.greenlink.admin.adapter.out.persistence.point;

import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.port.PointTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PointTransactionRepositoryAdapter implements PointTransactionRepository {

    private final PointTransactionJpaRepository jpaRepository;

    public PointTransactionRepositoryAdapter(PointTransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PointTransaction save(PointTransaction transaction) {
        return jpaRepository.save(PointTransactionJpaEntity.from(transaction)).toDomain();
    }

    @Override
    public Page<PointTransaction> findByCompanyId(Long companyId, Pageable pageable) {
        return jpaRepository.findByCompanyId(companyId, pageable).map(PointTransactionJpaEntity::toDomain);
    }
}
