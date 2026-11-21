package com.greenlink.core.domain.customerorder.port;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;

import java.util.Optional;

public interface CustomerOrderRepository {

    /**
     * UK 위반 시 false 반환 (중복 수집 방지).
     * coupang_order_details의 (coupang_order_id, coupang_shipment_box_id) UK 기준.
     */
    boolean insertIfAbsent(CustomerOrder order);

    CustomerOrder save(CustomerOrder order);

    Optional<CustomerOrder> findById(Long id, Long callerCompanyId);

    PageResult<CustomerOrder> findByCompany(Long companyId, OrderChannel channel,
                                            CustomerOrderStatus status, int page, int size);

    long countPending(Long companyId, OrderChannel channel);
}
