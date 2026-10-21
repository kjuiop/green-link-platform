package com.greenlink.core.application.orderhistory.service;

import com.greenlink.core.application.orderhistory.dto.OrderHistoryResult;
import com.greenlink.core.application.orderhistory.usecase.GetOrderHistoryUseCase;
import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.domain.orderhistory.model.OrderHistory;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.domain.orderhistory.port.OrderHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderHistoryService implements RecordOrderHistoryUseCase, GetOrderHistoryUseCase {

    private final OrderHistoryRepository orderHistoryRepository;

    public OrderHistoryService(OrderHistoryRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Override
    @Transactional
    public void record(Long orderId, OrderHistoryAction action, String detail) {
        orderHistoryRepository.save(OrderHistory.create(orderId, action, detail));
    }

    @Override
    public List<OrderHistoryResult> getHistory(Long orderId) {
        return orderHistoryRepository.findAllByOrderId(orderId).stream()
                .map(h -> new OrderHistoryResult(h.getId(), h.getOrderId(), h.getAction(), h.getDetail(), h.getCreatedAt()))
                .toList();
    }
}
