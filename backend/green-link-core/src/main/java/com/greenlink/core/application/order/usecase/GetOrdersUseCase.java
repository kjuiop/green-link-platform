package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetOrdersUseCase {

    List<OrderResult> getOrders(OrderSearchCondition condition);

    PageResult<OrderResult> getOrdersPaged(OrderSearchCondition condition, Pageable pageable);
}
