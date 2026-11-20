package com.greenlink.core.application.shop.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.shop.dto.ShopOrderResult;
import com.greenlink.core.application.shop.usecase.QueryShopOrdersUseCase;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.model.ShopOrderStatus;
import com.greenlink.core.domain.shop.port.ShopOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopOrderQueryService implements QueryShopOrdersUseCase {

    private final ShopOrderRepository orderRepository;
    private final OrderRepository linkedOrderRepository;

    public ShopOrderQueryService(ShopOrderRepository orderRepository, OrderRepository linkedOrderRepository) {
        this.orderRepository = orderRepository;
        this.linkedOrderRepository = linkedOrderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ShopOrderResult> list(Long callerCompanyId, Long filterCompanyId, ShopOrderStatus status,
                                            int page, int size) {
        Long companyId = callerCompanyId != null ? callerCompanyId : filterCompanyId;
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageResult.of(orderRepository.search(companyId, status, PageRequest.of(Math.max(page, 0), safeSize))
                .map(ShopOrderResult::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ShopOrderResult get(Long callerCompanyId, Long id) {
        ShopOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.ORDER_NOT_FOUND));
        ShopOrderAccess.check(callerCompanyId, order);
        Long linkedAmount = order.getLinkedOrderId() == null ? null
                : linkedOrderRepository.findById(order.getLinkedOrderId()).map(Order::getTotalAmount).orElse(null);
        return ShopOrderResult.from(order, linkedAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public long pendingCount(Long callerCompanyId, Long filterCompanyId) {
        Long companyId = callerCompanyId != null ? callerCompanyId : filterCompanyId;
        return orderRepository.countByStatus(companyId, ShopOrderStatus.PENDING);
    }
}
