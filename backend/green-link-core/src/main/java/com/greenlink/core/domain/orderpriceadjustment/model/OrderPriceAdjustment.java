package com.greenlink.core.domain.orderpriceadjustment.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;

public class OrderPriceAdjustment {

    public static final int MAX_REASON_LENGTH = 500;

    private Long id;
    private Long orderId;
    private Long requesterId;
    private String requesterName;
    private boolean requestedByProxy;
    private String requestedAt;
    private long beforeTotal;
    private long adjustAmount;
    private long afterTotal;
    private String reason;
    private PriceAdjustmentStatus status;
    private Long decidedById;
    private String decidedByName;
    private boolean decidedByProxy;
    private String decidedAt;
    private String rejectReason;

    private OrderPriceAdjustment() {}

    public static OrderPriceAdjustment request(Long orderId, Long requesterId, String requesterName,
                                               boolean proxy, String now,
                                               long beforeTotal, long afterTotal, String reason) {
        if (afterTotal < 0 || afterTotal == beforeTotal) {
            throw new BusinessException(OrderErrorCode.ADJUST_INVALID_AMOUNT);
        }
        String trimmed = requireReason(reason);
        OrderPriceAdjustment a = new OrderPriceAdjustment();
        a.orderId = orderId;
        a.requesterId = requesterId;
        a.requesterName = requesterName;
        a.requestedByProxy = proxy;
        a.requestedAt = now;
        a.beforeTotal = beforeTotal;
        a.afterTotal = afterTotal;
        a.adjustAmount = afterTotal - beforeTotal;
        a.reason = trimmed;
        a.status = PriceAdjustmentStatus.PENDING;
        return a;
    }

    public static OrderPriceAdjustment of(Long id, Long orderId, Long requesterId, String requesterName,
                                          boolean requestedByProxy, String requestedAt,
                                          long beforeTotal, long adjustAmount, long afterTotal, String reason,
                                          PriceAdjustmentStatus status, Long decidedById, String decidedByName,
                                          boolean decidedByProxy, String decidedAt, String rejectReason) {
        OrderPriceAdjustment a = new OrderPriceAdjustment();
        a.id = id;
        a.orderId = orderId;
        a.requesterId = requesterId;
        a.requesterName = requesterName;
        a.requestedByProxy = requestedByProxy;
        a.requestedAt = requestedAt;
        a.beforeTotal = beforeTotal;
        a.adjustAmount = adjustAmount;
        a.afterTotal = afterTotal;
        a.reason = reason;
        a.status = status;
        a.decidedById = decidedById;
        a.decidedByName = decidedByName;
        a.decidedByProxy = decidedByProxy;
        a.decidedAt = decidedAt;
        a.rejectReason = rejectReason;
        return a;
    }

    public void accept(Long deciderId, String deciderName, boolean proxy, String now) {
        requirePending();
        this.status = PriceAdjustmentStatus.ACCEPTED;
        decide(deciderId, deciderName, proxy, now);
    }

    public void reject(Long deciderId, String deciderName, boolean proxy, String now, String rejectReason) {
        requirePending();
        this.rejectReason = requireReason(rejectReason);
        this.status = PriceAdjustmentStatus.REJECTED;
        decide(deciderId, deciderName, proxy, now);
    }

    public boolean isPending() {
        return status == PriceAdjustmentStatus.PENDING;
    }

    private void requirePending() {
        if (!isPending()) {
            throw new BusinessException(OrderErrorCode.ADJUST_NOT_PENDING);
        }
    }

    private void decide(Long deciderId, String deciderName, boolean proxy, String now) {
        this.decidedById = deciderId;
        this.decidedByName = deciderName;
        this.decidedByProxy = proxy;
        this.decidedAt = now;
    }

    private static String requireReason(String reason) {
        if (reason == null || reason.isBlank() || reason.trim().length() > MAX_REASON_LENGTH) {
            throw new BusinessException(OrderErrorCode.ADJUST_REASON_REQUIRED);
        }
        return reason.trim();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getRequesterId() { return requesterId; }
    public String getRequesterName() { return requesterName; }
    public boolean isRequestedByProxy() { return requestedByProxy; }
    public String getRequestedAt() { return requestedAt; }
    public long getBeforeTotal() { return beforeTotal; }
    public long getAdjustAmount() { return adjustAmount; }
    public long getAfterTotal() { return afterTotal; }
    public String getReason() { return reason; }
    public PriceAdjustmentStatus getStatus() { return status; }
    public Long getDecidedById() { return decidedById; }
    public String getDecidedByName() { return decidedByName; }
    public boolean isDecidedByProxy() { return decidedByProxy; }
    public String getDecidedAt() { return decidedAt; }
    public String getRejectReason() { return rejectReason; }
}
