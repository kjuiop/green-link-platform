package com.greenlink.admin.adapter.out.persistence.customerorder;

import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.model.CustomerOrderSlice;
import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;
import com.greenlink.core.domain.customerorder.port.CustomerOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class CustomerOrderRepositoryAdapter implements CustomerOrderRepository {

    private final CustomerOrderJpaRepository jpaRepository;
    private final CoupangOrderDetailJpaRepository detailJpaRepository;
    private final CustomerOrderQueryRepository queryRepository;

    public CustomerOrderRepositoryAdapter(CustomerOrderJpaRepository jpaRepository,
                                          CoupangOrderDetailJpaRepository detailJpaRepository,
                                          CustomerOrderQueryRepository queryRepository) {
        this.jpaRepository = jpaRepository;
        this.detailJpaRepository = detailJpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public boolean insertIfAbsent(CustomerOrder order) {
        CustomerOrder.CoupangMeta meta = order.getCoupangMeta();
        if (meta != null && detailJpaRepository.existsByCoupangOrderIdAndCoupangShipmentBoxId(
                meta.coupangOrderId(), meta.coupangShipmentBoxId())) {
            return false;
        }
        try {
            CustomerOrderJpaEntity orderEntity = CustomerOrderJpaEntity.from(order);
            jpaRepository.saveAndFlush(orderEntity);
            if (meta != null) {
                CoupangOrderDetailJpaEntity detailEntity = CoupangOrderDetailJpaEntity.from(orderEntity, meta);
                detailJpaRepository.saveAndFlush(detailEntity);
            }
            return true;
        } catch (DataIntegrityViolationException e) {
            if (meta != null && detailJpaRepository.existsByCoupangOrderIdAndCoupangShipmentBoxId(
                    meta.coupangOrderId(), meta.coupangShipmentBoxId())) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public CustomerOrder save(CustomerOrder order) {
        CustomerOrderJpaEntity entity = order.getId() == null
                ? CustomerOrderJpaEntity.from(order)
                : jpaRepository.findById(order.getId())
                        .orElseThrow(() -> new BusinessException(CoupangErrorCode.ORDER_NOT_FOUND));
        entity.applyState(order);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<CustomerOrder> findById(Long id, Long callerCompanyId) {
        return jpaRepository.findByIdWithDetail(id)
                .filter(e -> callerCompanyId == null || callerCompanyId.equals(e.getCompanyId()))
                .map(CustomerOrderJpaEntity::toDomain);
    }

    @Override
    public CustomerOrderSlice findByCompany(Long companyId, OrderChannel channel,
                                            CustomerOrderStatus status, int page, int size) {
        return queryRepository.findByCompany(companyId, channel, status, page, size);
    }

    @Override
    public long countPending(Long companyId, OrderChannel channel) {
        return queryRepository.countPending(companyId, channel);
    }
}
