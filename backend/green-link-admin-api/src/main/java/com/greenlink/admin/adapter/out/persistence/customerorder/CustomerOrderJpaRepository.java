package com.greenlink.admin.adapter.out.persistence.customerorder;

import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerOrderJpaRepository extends JpaRepository<CustomerOrderJpaEntity, Long> {

    @Query("select o from CustomerOrderJpaEntity o left join fetch o.coupangDetail where o.id = :id")
    Optional<CustomerOrderJpaEntity> findByIdWithDetail(@Param("id") Long id);

    long countByCompanyIdAndChannelAndStatus(Long companyId, OrderChannel channel, CustomerOrderStatus status);

    long countByChannelAndStatus(OrderChannel channel, CustomerOrderStatus status);
}
