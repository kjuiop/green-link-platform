package com.greenlink.core.application.customerorder.service;

import com.greenlink.core.application.customerorder.dto.CustomerOrderResult;
import com.greenlink.core.application.customerorder.usecase.ProcessCustomerOrderUseCase;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.port.CustomerOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.customerorder.CustomerOrderErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerOrderStateService implements ProcessCustomerOrderUseCase {

    private final CustomerOrderRepository orderRepository;

    public CustomerOrderStateService(CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public CustomerOrderResult hold(Long callerCompanyId, Long id) {
        CustomerOrder order = loadForUpdate(callerCompanyId, id);
        order.hold();
        return CustomerOrderResult.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public CustomerOrderResult release(Long callerCompanyId, Long id) {
        CustomerOrder order = loadForUpdate(callerCompanyId, id);
        order.release();
        return CustomerOrderResult.from(orderRepository.save(order));
    }

    private CustomerOrder loadForUpdate(Long callerCompanyId, Long id) {
        CustomerOrder order = orderRepository.findById(id, callerCompanyId)
                .orElseThrow(() -> new BusinessException(CustomerOrderErrorCode.ORDER_NOT_FOUND));
        checkAccess(callerCompanyId, order);
        return order;
    }

    private void checkAccess(Long callerCompanyId, CustomerOrder order) {
        if (callerCompanyId != null && !callerCompanyId.equals(order.getCompanyId())) {
            throw new BusinessException(CustomerOrderErrorCode.FORBIDDEN_COMPANY);
        }
    }
}
