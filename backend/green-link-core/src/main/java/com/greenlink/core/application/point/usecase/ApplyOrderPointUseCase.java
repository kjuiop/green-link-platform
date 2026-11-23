package com.greenlink.core.application.point.usecase;

import com.greenlink.core.domain.point.model.OrderPointState;

public interface ApplyOrderPointUseCase {

    void holdForNewOrder(Long companyId, Long orderId, String orderNumber, long totalAmount, Long memberId);

    void syncOrder(Long companyId, Long orderId, String orderNumber,
                   OrderPointState state, long totalAmount, Long memberId);

    void restoreSettledOrder(Long companyId, Long orderId, String orderNumber, Long memberId);

    void adjustSettledOrder(Long companyId, Long orderId, String orderNumber, long delta, Long memberId);

    boolean canAdjustSettledOrder(Long companyId, Long orderId, long delta);

    boolean isSettled(Long orderId);
}
