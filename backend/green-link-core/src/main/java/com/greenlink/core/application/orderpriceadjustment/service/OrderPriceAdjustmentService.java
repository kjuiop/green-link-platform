package com.greenlink.core.application.orderpriceadjustment.service;

import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.application.orderpriceadjustment.dto.PriceAdjustmentResult;
import com.greenlink.core.application.orderpriceadjustment.usecase.DecidePriceAdjustmentUseCase;
import com.greenlink.core.application.orderpriceadjustment.usecase.GetPriceAdjustmentsUseCase;
import com.greenlink.core.application.orderpriceadjustment.usecase.RequestPriceAdjustmentUseCase;
import com.greenlink.core.application.point.usecase.ApplyOrderPointUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.domain.orderpriceadjustment.model.OrderPriceAdjustment;
import com.greenlink.core.domain.orderpriceadjustment.port.OrderPriceAdjustmentRepository;
import com.greenlink.core.domain.settlement.port.SettlementRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import com.greenlink.core.global.exception.point.PointErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class OrderPriceAdjustmentService implements
        RequestPriceAdjustmentUseCase, DecidePriceAdjustmentUseCase, GetPriceAdjustmentsUseCase {

    private final OrderRepository orderRepository;
    private final OrderPriceAdjustmentRepository adjustmentRepository;
    private final SettlementRepository settlementRepository;
    private final ApplyOrderPointUseCase applyOrderPointUseCase;
    private final RecordOrderHistoryUseCase recordOrderHistoryUseCase;
    private final AdminRepository adminRepository;

    public OrderPriceAdjustmentService(OrderRepository orderRepository,
                                       OrderPriceAdjustmentRepository adjustmentRepository,
                                       SettlementRepository settlementRepository,
                                       ApplyOrderPointUseCase applyOrderPointUseCase,
                                       RecordOrderHistoryUseCase recordOrderHistoryUseCase,
                                       AdminRepository adminRepository) {
        this.orderRepository = orderRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.settlementRepository = settlementRepository;
        this.applyOrderPointUseCase = applyOrderPointUseCase;
        this.recordOrderHistoryUseCase = recordOrderHistoryUseCase;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public PriceAdjustmentResult request(Long orderId, long newTotalAmount, String reason, boolean proxy) {
        Order order = lockActiveOrder(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(OrderErrorCode.ADJUST_ONLY_DELIVERED);
        }
        if (adjustmentRepository.findPendingByOrderId(orderId).isPresent()) {
            throw new BusinessException(OrderErrorCode.ADJUST_PENDING_EXISTS);
        }
        assertSettlementNotCompleted(order);

        Long actorId = resolveCurrentAdminId();
        OrderPriceAdjustment adjustment = OrderPriceAdjustment.request(
                orderId, actorId, resolveName(actorId), proxy, kstNow(),
                order.getTotalAmount(), newTotalAmount, reason);
        assertPointAvailable(order, adjustment.getAdjustAmount());

        OrderPriceAdjustment saved = adjustmentRepository.save(adjustment);
        recordOrderHistoryUseCase.record(orderId, OrderHistoryAction.PRICE_ADJUSTED,
                "금액 수정 요청 " + signed(saved.getAdjustAmount()) + " (사유: " + saved.getReason() + ")" + proxySuffix(proxy));
        return toResult(saved);
    }

    @Override
    @Transactional
    public PriceAdjustmentResult accept(Long orderId, Long adjustmentId, boolean proxy) {
        Order order = lockActiveOrder(orderId);
        OrderPriceAdjustment adjustment = findAdjustment(orderId, adjustmentId);
        if (!adjustment.isPending()) {
            throw new BusinessException(OrderErrorCode.ADJUST_NOT_PENDING);
        }
        if (order.getTotalAmount() != adjustment.getBeforeTotal()) {
            throw new BusinessException(OrderErrorCode.ADJUST_TOTAL_CHANGED);
        }
        assertSettlementNotCompleted(order);

        order.applyAdjustment(adjustment.getAdjustAmount());
        orderRepository.save(order);
        if (order.getOrderingCompanyId() != null) {
            applyOrderPointUseCase.adjustSettledOrder(order.getOrderingCompanyId(), orderId,
                    order.getOrderNumber(), adjustment.getAdjustAmount(), resolveCurrentAdminId());
        }

        Long actorId = resolveCurrentAdminId();
        adjustment.accept(actorId, resolveName(actorId), proxy, kstNow());
        OrderPriceAdjustment saved = adjustmentRepository.save(adjustment);
        recordOrderHistoryUseCase.record(orderId, OrderHistoryAction.PRICE_ADJUSTED,
                "금액 수정 승인 " + signed(saved.getAdjustAmount()) + proxySuffix(proxy));
        return toResult(saved);
    }

    @Override
    @Transactional
    public PriceAdjustmentResult reject(Long orderId, Long adjustmentId, String reason, boolean proxy) {
        lockActiveOrder(orderId);
        OrderPriceAdjustment adjustment = findAdjustment(orderId, adjustmentId);
        Long actorId = resolveCurrentAdminId();
        adjustment.reject(actorId, resolveName(actorId), proxy, kstNow(), reason);
        OrderPriceAdjustment saved = adjustmentRepository.save(adjustment);
        recordOrderHistoryUseCase.record(orderId, OrderHistoryAction.PRICE_ADJUSTED,
                "금액 수정 거절 " + signed(saved.getAdjustAmount()) + " (사유: " + saved.getRejectReason() + ")" + proxySuffix(proxy));
        return toResult(saved);
    }

    @Override
    public List<PriceAdjustmentResult> getAdjustments(Long orderId) {
        List<PriceAdjustmentResult> results = new ArrayList<>();
        for (OrderPriceAdjustment a : adjustmentRepository.findAllByOrderId(orderId)) {
            results.add(toResult(a));
        }
        return results;
    }

    private Order lockActiveOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        if (order.isDeleted()) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private OrderPriceAdjustment findAdjustment(Long orderId, Long adjustmentId) {
        return adjustmentRepository.findById(adjustmentId)
                .filter(a -> Objects.equals(a.getOrderId(), orderId))
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ADJUST_NOT_FOUND));
    }

    private void assertSettlementNotCompleted(Order order) {
        String completedAt = order.getCompletedAt();
        if (completedAt == null || completedAt.length() < 10) return;
        List<Long> companyIds = new ArrayList<>();
        if (order.getVendorCompanyId() != null) companyIds.add(order.getVendorCompanyId());
        if (order.getOrderingCompanyId() != null) companyIds.add(order.getOrderingCompanyId());
        if (companyIds.isEmpty()) return;
        if (settlementRepository.existsCompletedCovering(companyIds, completedAt.substring(0, 10))) {
            throw new BusinessException(OrderErrorCode.ADJUST_SETTLED_LOCKED);
        }
    }

    private void assertPointAvailable(Order order, long delta) {
        if (order.getOrderingCompanyId() == null) return;
        if (!applyOrderPointUseCase.canAdjustSettledOrder(order.getOrderingCompanyId(), order.getId(), delta)) {
            throw new BusinessException(PointErrorCode.INSUFFICIENT_POINT);
        }
    }

    private PriceAdjustmentResult toResult(OrderPriceAdjustment a) {
        return new PriceAdjustmentResult(a.getId(), a.getOrderId(), a.getRequesterName(), a.isRequestedByProxy(),
                a.getRequestedAt(), a.getBeforeTotal(), a.getAdjustAmount(), a.getAfterTotal(), a.getReason(),
                a.getStatus().name(), a.getDecidedByName(), a.isDecidedByProxy(), a.getDecidedAt(), a.getRejectReason());
    }

    private String resolveName(Long adminId) {
        if (adminId == null) return null;
        return adminRepository.findById(adminId).map(Admin::getName).orElse(null);
    }

    private static String signed(long delta) {
        return (delta > 0 ? "+" : "-") + String.format("%,d", Math.abs(delta)) + "원";
    }

    private static String proxySuffix(boolean proxy) {
        return proxy ? " (관리자 대행)" : "";
    }

    private static Long resolveCurrentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        try {
            return Long.parseLong(String.valueOf(auth.getPrincipal()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String kstNow() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
