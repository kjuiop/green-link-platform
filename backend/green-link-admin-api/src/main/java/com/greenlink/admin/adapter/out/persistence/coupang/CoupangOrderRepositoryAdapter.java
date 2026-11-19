package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.model.CoupangOrderStatus;
import com.greenlink.core.domain.coupang.port.CoupangOrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CoupangOrderRepositoryAdapter implements CoupangOrderRepository {

    private final CoupangOrderJpaRepository jpaRepository;

    public CoupangOrderRepositoryAdapter(CoupangOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CoupangOrder> findById(Long id) {
        return jpaRepository.findById(id).map(CoupangOrderJpaEntity::toDomain);
    }

    @Override
    public Optional<CoupangOrder> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id).map(CoupangOrderJpaEntity::toDomain);
    }

    @Override
    public boolean existsByCompanyIdAndShipmentBoxId(Long companyId, long shipmentBoxId) {
        return jpaRepository.existsByCompanyIdAndShipmentBoxId(companyId, shipmentBoxId);
    }

    @Override
    public boolean insertIfAbsent(CoupangOrder order) {
        if (jpaRepository.existsByCompanyIdAndShipmentBoxId(order.getCompanyId(), order.getShipmentBoxId())) {
            return false;
        }
        try {
            jpaRepository.saveAndFlush(CoupangOrderJpaEntity.from(order));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Override
    public CoupangOrder save(CoupangOrder order) {
        CoupangOrderJpaEntity entity = order.getId() == null
                ? CoupangOrderJpaEntity.from(order)
                : jpaRepository.findById(order.getId()).orElseGet(() -> CoupangOrderJpaEntity.from(order));
        entity.applyState(order);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Page<CoupangOrder> search(Long companyId, CoupangOrderStatus status, Pageable pageable) {
        Page<CoupangOrderJpaEntity> page;
        if (companyId != null && status != null) {
            page = jpaRepository.findByCompanyIdAndStatusOrderByOrderedAtDescIdDesc(companyId, status, pageable);
        } else if (companyId != null) {
            page = jpaRepository.findByCompanyIdOrderByOrderedAtDescIdDesc(companyId, pageable);
        } else if (status != null) {
            page = jpaRepository.findByStatusOrderByOrderedAtDescIdDesc(status, pageable);
        } else {
            page = jpaRepository.findAllByOrderByOrderedAtDescIdDesc(pageable);
        }
        return page.map(CoupangOrderJpaEntity::toDomain);
    }

    @Override
    public long countByStatus(Long companyId, CoupangOrderStatus status) {
        return companyId != null
                ? jpaRepository.countByCompanyIdAndStatus(companyId, status)
                : jpaRepository.countByStatus(status);
    }
}
