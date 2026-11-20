package com.greenlink.core.domain.taxinvoice.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaxInvoice {

    private static final int MAX_REASON_LENGTH = 400;
    private static final Duration STALE_AFTER = Duration.ofMinutes(5);

    private Long id;
    private Long pointTransactionId;
    private Long companyId;
    private TaxInvoiceStatus status;
    private long supplyAmount;
    private long taxAmount;
    private int attempt;
    private String mgtNum;
    private String docKey;
    private String failReason;
    private String recipientEmail;
    private Long requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime issuedAt;

    private TaxInvoice() {}

    public static TaxInvoice start(Long pointTransactionId, Long companyId, long supplyAmount,
                                   String recipientEmail, Long requestedBy, LocalDateTime now) {
        TaxInvoice t = new TaxInvoice();
        t.pointTransactionId = pointTransactionId;
        t.companyId = companyId;
        t.supplyAmount = supplyAmount;
        t.taxAmount = 0L;
        t.recipientEmail = recipientEmail;
        t.requestedBy = requestedBy;
        t.status = TaxInvoiceStatus.ISSUING;
        t.attempt = 1;
        t.mgtNum = mgtNumOf(pointTransactionId, 1);
        t.requestedAt = now;
        return t;
    }

    public static TaxInvoice of(Long id, Long pointTransactionId, Long companyId, TaxInvoiceStatus status,
                                long supplyAmount, long taxAmount, int attempt, String mgtNum, String docKey,
                                String failReason, String recipientEmail, Long requestedBy,
                                LocalDateTime requestedAt, LocalDateTime issuedAt) {
        TaxInvoice t = new TaxInvoice();
        t.id = id;
        t.pointTransactionId = pointTransactionId;
        t.companyId = companyId;
        t.status = status;
        t.supplyAmount = supplyAmount;
        t.taxAmount = taxAmount;
        t.attempt = attempt;
        t.mgtNum = mgtNum;
        t.docKey = docKey;
        t.failReason = failReason;
        t.recipientEmail = recipientEmail;
        t.requestedBy = requestedBy;
        t.requestedAt = requestedAt;
        t.issuedAt = issuedAt;
        return t;
    }

    public static String mgtNumOf(Long pointTransactionId, int attempt) {
        return "GL-" + pointTransactionId + "-" + attempt;
    }

    /** 실패한 건만 새 시도로 전환한다. 시도마다 관리번호가 달라 바로빌의 관리번호 중복 오류를 피한다. */
    public void restart(String recipientEmail, Long requestedBy, LocalDateTime now) {
        if (status != TaxInvoiceStatus.FAILED) {
            throw new IllegalStateException("실패한 계산서만 재시도할 수 있습니다.");
        }
        this.attempt += 1;
        this.mgtNum = mgtNumOf(pointTransactionId, attempt);
        this.status = TaxInvoiceStatus.ISSUING;
        this.failReason = null;
        this.recipientEmail = recipientEmail;
        this.requestedBy = requestedBy;
        this.requestedAt = now;
    }

    public void markSuccess(String docKey, LocalDateTime now) {
        this.status = TaxInvoiceStatus.SUCCESS;
        this.docKey = docKey;
        this.failReason = null;
        this.issuedAt = now;
    }

    public void markFailed(String reason) {
        this.status = TaxInvoiceStatus.FAILED;
        this.failReason = reason != null && reason.length() > MAX_REASON_LENGTH ? reason.substring(0, MAX_REASON_LENGTH) : reason;
    }

    public boolean isStaleIssuing(LocalDateTime now) {
        return status == TaxInvoiceStatus.ISSUING && requestedAt != null
                && requestedAt.plus(STALE_AFTER).isBefore(now);
    }

    public Long getId() { return id; }
    public Long getPointTransactionId() { return pointTransactionId; }
    public Long getCompanyId() { return companyId; }
    public TaxInvoiceStatus getStatus() { return status; }
    public long getSupplyAmount() { return supplyAmount; }
    public long getTaxAmount() { return taxAmount; }
    public int getAttempt() { return attempt; }
    public String getMgtNum() { return mgtNum; }
    public String getDocKey() { return docKey; }
    public String getFailReason() { return failReason; }
    public String getRecipientEmail() { return recipientEmail; }
    public Long getRequestedBy() { return requestedBy; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
}
