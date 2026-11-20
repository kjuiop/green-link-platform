package com.greenlink.core.domain.fax.model;

import java.time.LocalDateTime;

public class FaxMessage {

    private static final int MAX_REASON_LENGTH = 200;

    private Long id;
    private Long orderId;
    private int sequence;
    private Long vendorCompanyId;
    private String toNumber;
    private String contentHash;
    private FaxStatus status;
    private String sendKey;
    private String failReason;
    private boolean manual;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    private FaxMessage() {}

    public static FaxMessage start(Long orderId, int sequence, Long vendorCompanyId, String toNumber,
                                   String contentHash, boolean manual, LocalDateTime now) {
        FaxMessage m = new FaxMessage();
        m.orderId = orderId;
        m.sequence = sequence;
        m.vendorCompanyId = vendorCompanyId;
        m.toNumber = toNumber;
        m.contentHash = contentHash;
        m.manual = manual;
        m.status = FaxStatus.SENDING;
        m.requestedAt = now;
        return m;
    }

    public static FaxMessage of(Long id, Long orderId, int sequence, Long vendorCompanyId, String toNumber,
                                String contentHash, FaxStatus status, String sendKey, String failReason,
                                boolean manual, LocalDateTime requestedAt, LocalDateTime completedAt) {
        FaxMessage m = new FaxMessage();
        m.id = id;
        m.orderId = orderId;
        m.sequence = sequence;
        m.vendorCompanyId = vendorCompanyId;
        m.toNumber = toNumber;
        m.contentHash = contentHash;
        m.status = status;
        m.sendKey = sendKey;
        m.failReason = failReason;
        m.manual = manual;
        m.requestedAt = requestedAt;
        m.completedAt = completedAt;
        return m;
    }

    public void markSubmitted(String sendKey) {
        this.status = FaxStatus.SUBMITTED;
        this.sendKey = sendKey;
        this.failReason = null;
    }

    public void markDelivered(LocalDateTime now) {
        this.status = FaxStatus.DELIVERED;
        this.completedAt = now;
    }

    public void markFailed(String reason, LocalDateTime now) {
        this.status = FaxStatus.FAILED;
        this.failReason = reason != null && reason.length() > MAX_REASON_LENGTH ? reason.substring(0, MAX_REASON_LENGTH) : reason;
        this.completedAt = now;
    }

    public boolean isEffective() {
        return status != FaxStatus.FAILED;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public int getSequence() { return sequence; }
    public Long getVendorCompanyId() { return vendorCompanyId; }
    public String getToNumber() { return toNumber; }
    public String getContentHash() { return contentHash; }
    public FaxStatus getStatus() { return status; }
    public String getSendKey() { return sendKey; }
    public String getFailReason() { return failReason; }
    public boolean isManual() { return manual; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
