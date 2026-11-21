package com.greenlink.core.application.customerorder.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.customerorder.dto.CustomerOrderResult;
import com.greenlink.core.application.customerorder.usecase.QueryCustomerOrdersUseCase;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;
import com.greenlink.core.domain.customerorder.port.CustomerOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.customerorder.CustomerOrderErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerOrderQueryService implements QueryCustomerOrdersUseCase {

    private final CustomerOrderRepository orderRepository;

    public CustomerOrderQueryService(CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CustomerOrderResult> list(Long callerCompanyId, Long companyId, OrderChannel channel,
                                                CustomerOrderStatus status, int page, int size) {
        Long effectiveCompanyId = effectiveCompanyId(callerCompanyId, companyId);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageResult<CustomerOrder> raw = orderRepository.findByCompany(
                effectiveCompanyId, channel, status, Math.max(page, 0), safeSize);
        return new PageResult<>(
                raw.content().stream().map(CustomerOrderResult::from).toList(),
                raw.page(), raw.size(), raw.totalElements(), raw.totalPages(), raw.totalAmount()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderResult get(Long callerCompanyId, Long id) {
        CustomerOrder order = orderRepository.findById(id, callerCompanyId)
                .orElseThrow(() -> new BusinessException(CustomerOrderErrorCode.ORDER_NOT_FOUND));
        return CustomerOrderResult.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public long pendingCount(Long callerCompanyId, Long companyId, OrderChannel channel) {
        return orderRepository.countPending(effectiveCompanyId(callerCompanyId, companyId), channel);
    }

    private Long effectiveCompanyId(Long callerCompanyId, Long filterCompanyId) {
        return callerCompanyId != null ? callerCompanyId : filterCompanyId;
    }
}
