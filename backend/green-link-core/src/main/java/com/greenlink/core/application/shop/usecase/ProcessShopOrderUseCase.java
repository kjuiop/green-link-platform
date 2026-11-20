package com.greenlink.core.application.shop.usecase;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.shop.dto.ShopOrderResult;

import java.util.function.Function;

public interface ProcessShopOrderUseCase {

    /**
     * commandFactory: 발주사(orderingCompanyId, 수주사 전용 회사면 null)를 받아 발주 생성 명령을 만든다.
     * 수주사 전용 회사는 본인 회사 소속 계정으로만 발주할 수 있다.
     */
    OrderResult approve(Long callerCompanyId, Long id, Long adminId, Function<Long, CreateOrderCommand> commandFactory);

    ShopOrderResult hold(Long callerCompanyId, Long id);

    ShopOrderResult release(Long callerCompanyId, Long id);

    ShopOrderResult reject(Long callerCompanyId, Long id, String reason);
}
