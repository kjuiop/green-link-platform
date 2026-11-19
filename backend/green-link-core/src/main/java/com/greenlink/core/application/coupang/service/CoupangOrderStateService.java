package com.greenlink.core.application.coupang.service;

import com.greenlink.core.application.coupang.dto.CoupangOrderResult;
import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.port.CoupangOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.LongFunction;

/** 쿠팡 주문 상태 전이를 트랜잭션 단위로 처리한다. 외부 API 호출은 포함하지 않는다. */
@Service
public class CoupangOrderStateService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final CoupangOrderRepository orderRepository;
    private final CreateOrderUseCase createOrderUseCase;

    public CoupangOrderStateService(CoupangOrderRepository orderRepository, CreateOrderUseCase createOrderUseCase) {
        this.orderRepository = orderRepository;
        this.createOrderUseCase = createOrderUseCase;
    }

    @Transactional(readOnly = true)
    public CoupangOrder loadApprovable(Long callerCompanyId, Long id) {
        CoupangOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.ORDER_NOT_FOUND));
        CoupangOrderAccess.check(callerCompanyId, order);
        if (!order.isApprovable()) {
            throw new BusinessException(CoupangErrorCode.NOT_APPROVABLE);
        }
        return order;
    }

    /** 발주 생성 실패 시 전체 롤백되어 쿠팡 주문은 원래 상태로 남는다. */
    @Transactional
    public OrderResult approve(Long callerCompanyId, Long id, Long adminId,
                               LongFunction<CreateOrderCommand> commandFactory) {
        CoupangOrder order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.ORDER_NOT_FOUND));
        CoupangOrderAccess.check(callerCompanyId, order);
        if (!order.isApprovable()) {
            throw new BusinessException(CoupangErrorCode.NOT_APPROVABLE);
        }
        OrderResult created = createOrderUseCase.createOrder(commandFactory.apply(order.getCompanyId()));
        order.approve(created.id(), adminId, LocalDateTime.now(KST));
        orderRepository.save(order);
        return created;
    }

    @Transactional
    public CoupangOrderResult hold(Long callerCompanyId, Long id) {
        CoupangOrder order = lock(callerCompanyId, id);
        order.hold();
        return CoupangOrderResult.from(orderRepository.save(order));
    }

    @Transactional
    public CoupangOrderResult release(Long callerCompanyId, Long id) {
        CoupangOrder order = lock(callerCompanyId, id);
        order.release();
        return CoupangOrderResult.from(orderRepository.save(order));
    }

    @Transactional
    public void markCanceled(Long id) {
        CoupangOrder order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.ORDER_NOT_FOUND));
        if (order.isApprovable()) {
            order.markCanceled();
            orderRepository.save(order);
        }
    }

    private CoupangOrder lock(Long callerCompanyId, Long id) {
        CoupangOrder order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.ORDER_NOT_FOUND));
        CoupangOrderAccess.check(callerCompanyId, order);
        return order;
    }
}
