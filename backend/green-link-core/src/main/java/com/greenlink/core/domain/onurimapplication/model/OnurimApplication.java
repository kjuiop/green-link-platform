package com.greenlink.core.domain.onurimapplication.model;

import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.onurimapplication.OnurimApplicationErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public class OnurimApplication {

    public static final int MAX_REASON_LENGTH = 500;

    private Long id;
    private Long companyId;
    private Long applicantAdminId;
    private CompanyType desiredType;
    private OnurimApplicationStatus status;
    private OnurimApplicationInfo info;
    private String businessRegUrl;
    private String bankBookUrl;
    private CompanyType approvedType;
    private String rejectReason;
    private Long processedBy;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    private OnurimApplication() {}

    public static OnurimApplication request(Long companyId, Long applicantAdminId,
                                            CompanyType desiredType, OnurimApplicationInfo info,
                                            String businessRegUrl, String bankBookUrl) {
        if (desiredType == null || info == null || isBlank(info.companyPhone()) || isBlank(info.address())) {
            throw new BusinessException(OnurimApplicationErrorCode.INVALID_APPLICATION);
        }
        OnurimApplication a = new OnurimApplication();
        a.companyId = companyId;
        a.applicantAdminId = applicantAdminId;
        a.desiredType = desiredType;
        a.info = normalize(info);
        a.businessRegUrl = businessRegUrl;
        a.bankBookUrl = bankBookUrl;
        a.status = OnurimApplicationStatus.PENDING;
        return a;
    }

    public static OnurimApplication of(Long id, Long companyId, Long applicantAdminId, CompanyType desiredType,
                                       OnurimApplicationStatus status, OnurimApplicationInfo info,
                                       String businessRegUrl, String bankBookUrl, CompanyType approvedType, String rejectReason,
                                       Long processedBy, LocalDateTime processedAt, LocalDateTime createdAt) {
        OnurimApplication a = new OnurimApplication();
        a.id = id;
        a.companyId = companyId;
        a.applicantAdminId = applicantAdminId;
        a.desiredType = desiredType;
        a.status = status;
        a.info = info;
        a.businessRegUrl = businessRegUrl;
        a.bankBookUrl = bankBookUrl;
        a.approvedType = approvedType;
        a.rejectReason = rejectReason;
        a.processedBy = processedBy;
        a.processedAt = processedAt;
        a.createdAt = createdAt;
        return a;
    }

    public void approve(CompanyType type, Long operatorId, LocalDateTime now) {
        requirePending();
        if (type == null) {
            throw new BusinessException(OnurimApplicationErrorCode.TYPE_REQUIRED);
        }
        this.status = OnurimApplicationStatus.APPROVED;
        this.approvedType = type;
        decide(operatorId, now);
    }

    public void reject(String reason, Long operatorId, LocalDateTime now) {
        requirePending();
        if (reason == null || reason.isBlank() || reason.trim().length() > MAX_REASON_LENGTH) {
            throw new BusinessException(OnurimApplicationErrorCode.REJECT_REASON_REQUIRED);
        }
        this.status = OnurimApplicationStatus.REJECTED;
        this.rejectReason = reason.trim();
        decide(operatorId, now);
    }

    public boolean isPending() {
        return status == OnurimApplicationStatus.PENDING;
    }

    private void requirePending() {
        if (!isPending()) {
            throw new BusinessException(OnurimApplicationErrorCode.NOT_PENDING);
        }
    }

    private void decide(Long operatorId, LocalDateTime now) {
        this.processedBy = operatorId;
        this.processedAt = now;
    }

    private static OnurimApplicationInfo normalize(OnurimApplicationInfo i) {
        String fax = i.faxNumber() == null ? "" : i.faxNumber().replaceAll("[^0-9]", "");
        List<OnurimApplicationRegion> regions = i.regions() == null ? List.of() : i.regions().stream()
            .filter(r -> r != null && !isBlank(r.sido()))
            .map(r -> new OnurimApplicationRegion(r.sido().trim(), isBlank(r.sigungu()) ? null : r.sigungu().trim()))
            .distinct()
            .toList();
        return new OnurimApplicationInfo(
            trim(i.companyPhone()), trim(i.address()), trim(i.detailAddress()), trim(i.taxInvoiceEmail()),
            trim(i.memo()), fax.isEmpty() ? null : fax,
            trim(i.bankName()), trim(i.accountNumber()), trim(i.accountHolder()), regions);
    }

    private static String trim(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public Long getApplicantAdminId() { return applicantAdminId; }
    public CompanyType getDesiredType() { return desiredType; }
    public OnurimApplicationStatus getStatus() { return status; }
    public OnurimApplicationInfo getInfo() { return info; }
    public String getBusinessRegUrl() { return businessRegUrl; }
    public String getBankBookUrl() { return bankBookUrl; }
    public CompanyType getApprovedType() { return approvedType; }
    public String getRejectReason() { return rejectReason; }
    public Long getProcessedBy() { return processedBy; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
