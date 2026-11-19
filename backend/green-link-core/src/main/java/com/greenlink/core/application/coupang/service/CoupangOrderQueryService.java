package com.greenlink.core.application.coupang.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.coupang.dto.CoupangOrderResult;
import com.greenlink.core.application.coupang.usecase.QueryCoupangOrdersUseCase;
import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.model.CoupangOrderStatus;
import com.greenlink.core.domain.coupang.port.CoupangOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoupangOrderQueryService implements QueryCoupangOrdersUseCase {

    private final CoupangOrderRepository orderRepository;

    public CoupangOrderQueryService(CoupangOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CoupangOrderResult> list(Long callerCompanyId, Long filterCompanyId, CoupangOrderStatus status,
                                               int page, int size) {
        Long companyId = effectiveCompanyId(callerCompanyId, filterCompanyId);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageResult.of(orderRepository.search(companyId, status, PageRequest.of(Math.max(page, 0), safeSize))
                .map(CoupangOrderResult::from));
    }

    @Override
    @Transactional(readOnly = true)
    public CoupangOrderResult get(Long callerCompanyId, Long id) {
        CoupangOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.ORDER_NOT_FOUND));
        CoupangOrderAccess.check(callerCompanyId, order);
        return CoupangOrderResult.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public long pendingCount(Long callerCompanyId, Long filterCompanyId) {
        return orderRepository.countByStatus(effectiveCompanyId(callerCompanyId, filterCompanyId),
                CoupangOrderStatus.PENDING);
    }

    private Long effectiveCompanyId(Long callerCompanyId, Long filterCompanyId) {
        return callerCompanyId != null ? callerCompanyId : filterCompanyId;
    }
}
