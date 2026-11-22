package com.greenlink.core.application.coupang.usecase;

import com.greenlink.core.application.coupang.dto.CoupangOrderResult;
import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;

import java.util.function.LongFunction;

public interface ProcessCoupangOrderUseCase {

    /** commandFactory: 쿠팡 주문의 발주사 ID를 받아 발주 생성 명령을 만든다. */
    OrderResult approve(Long callerCompanyId, Long id, LongFunction<CreateOrderCommand> commandFactory);

    CoupangOrderResult hold(Long callerCompanyId, Long id);

    CoupangOrderResult release(Long callerCompanyId, Long id);
}
