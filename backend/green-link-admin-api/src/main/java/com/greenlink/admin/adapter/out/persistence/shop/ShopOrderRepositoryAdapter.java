package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.model.ShopOrderStatus;
import com.greenlink.core.domain.shop.port.ShopOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShopOrderRepositoryAdapter implements ShopOrderRepository {

    private final ShopOrderJpaRepository jpaRepository;

    public ShopOrderRepositoryAdapter(ShopOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ShopOrder> findById(Long id) {
        return jpaRepository.findById(id).map(ShopOrderJpaEntity::toDomain);
    }

    @Override
    public Optional<ShopOrder> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id).map(ShopOrderJpaEntity::toDomain);
    }

    @Override
    public Optional<ShopOrder> findByShopIdAndOrderNo(Long shopId, String orderNo) {
        return jpaRepository.findByShopIdAndOrderNo(shopId, orderNo).map(ShopOrderJpaEntity::toDomain);
    }

    @Override
    public boolean existsByOrderNo(String orderNo) {
        return jpaRepository.existsByOrderNo(orderNo);
    }

    @Override
    public ShopOrder save(ShopOrder order) {
        ShopOrderJpaEntity entity = order.getId() == null
                ? ShopOrderJpaEntity.from(order)
                : jpaRepository.findById(order.getId()).orElseGet(() -> ShopOrderJpaEntity.from(order));
        entity.applyState(order);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Page<ShopOrder> search(Long companyId, ShopOrderStatus status, Pageable pageable) {
        Page<ShopOrderJpaEntity> page;
        if (companyId != null && status != null) {
            page = jpaRepository.findByCompanyIdAndStatusOrderByCreatedAtDescIdDesc(companyId, status, pageable);
        } else if (companyId != null) {
            page = jpaRepository.findByCompanyIdOrderByCreatedAtDescIdDesc(companyId, pageable);
        } else if (status != null) {
            page = jpaRepository.findByStatusOrderByCreatedAtDescIdDesc(status, pageable);
        } else {
            page = jpaRepository.findAllByOrderByCreatedAtDescIdDesc(pageable);
        }
        return page.map(ShopOrderJpaEntity::toDomain);
    }

    @Override
    public long countByStatus(Long companyId, ShopOrderStatus status) {
        return companyId != null
                ? jpaRepository.countByCompanyIdAndStatus(companyId, status)
                : jpaRepository.countByStatus(status);
    }
}
