package com.greenlink.admin.adapter.out.persistence.onurimapplication;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplication;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationRegion;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "onurim_applications",
       indexes = @Index(name = "idx_onurim_applications_company_id", columnList = "company_id"),
       uniqueConstraints = @UniqueConstraint(name = "uk_onurim_applications_pending_company", columnNames = "pending_company_id"))
public class OnurimApplicationJpaEntity extends BaseJpaEntity {

    private static final String REGION_SEPARATOR = ",";
    private static final String SIGUNGU_SEPARATOR = "|";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "pending_company_id")
    private Long pendingCompanyId;

    @Column(name = "applicant_admin_id", nullable = false)
    private Long applicantAdminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "desired_type", nullable = false, length = 20)
    private CompanyType desiredType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OnurimApplicationStatus status;

    @Column(name = "company_phone", length = 30)
    private String companyPhone;

    @Column(length = 255)
    private String address;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "tax_invoice_email", length = 255)
    private String taxInvoiceEmail;

    @Column(length = 500)
    private String memo;

    @Column(name = "fax_number", length = 30)
    private String faxNumber;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "account_holder", length = 50)
    private String accountHolder;

    @Column(length = 4000)
    private String regions;

    @Column(name = "business_reg_url", length = 500)
    private String businessRegUrl;

    @Column(name = "bank_book_url", length = 500)
    private String bankBookUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "approved_type", length = 20)
    private CompanyType approvedType;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    protected OnurimApplicationJpaEntity() {}

    public static OnurimApplicationJpaEntity from(OnurimApplication a) {
        OnurimApplicationJpaEntity e = new OnurimApplicationJpaEntity();
        OnurimApplicationInfo i = a.getInfo();
        e.id = a.getId();
        e.companyId = a.getCompanyId();
        e.pendingCompanyId = a.isPending() ? a.getCompanyId() : null;
        e.applicantAdminId = a.getApplicantAdminId();
        e.desiredType = a.getDesiredType();
        e.status = a.getStatus();
        e.companyPhone = i.companyPhone();
        e.address = i.address();
        e.detailAddress = i.detailAddress();
        e.taxInvoiceEmail = i.taxInvoiceEmail();
        e.memo = i.memo();
        e.faxNumber = i.faxNumber();
        e.bankName = i.bankName();
        e.accountNumber = i.accountNumber();
        e.accountHolder = i.accountHolder();
        e.regions = joinRegions(i.regions());
        e.businessRegUrl = a.getBusinessRegUrl();
        e.bankBookUrl = a.getBankBookUrl();
        e.approvedType = a.getApprovedType();
        e.rejectReason = a.getRejectReason();
        e.processedBy = a.getProcessedBy();
        e.processedAt = a.getProcessedAt();
        return e;
    }

    public OnurimApplication toDomain() {
        OnurimApplicationInfo info = new OnurimApplicationInfo(companyPhone, address, detailAddress,
            taxInvoiceEmail, memo, faxNumber, bankName, accountNumber, accountHolder, splitRegions(regions));
        return OnurimApplication.of(id, companyId, applicantAdminId, desiredType, status, info,
            businessRegUrl, bankBookUrl, approvedType, rejectReason, processedBy, processedAt, getCreatedAt());
    }

    static String joinRegions(List<OnurimApplicationRegion> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(REGION_SEPARATOR, list.stream()
            .map(r -> r.sido() + (r.sigungu() == null ? "" : SIGUNGU_SEPARATOR + r.sigungu()))
            .toList());
    }

    static List<OnurimApplicationRegion> splitRegions(String value) {
        List<OnurimApplicationRegion> result = new ArrayList<>();
        if (value == null || value.isBlank()) return result;
        for (String token : value.split(REGION_SEPARATOR)) {
            if (token.isBlank()) continue;
            int idx = token.indexOf(SIGUNGU_SEPARATOR);
            result.add(idx < 0
                ? new OnurimApplicationRegion(token, null)
                : new OnurimApplicationRegion(token.substring(0, idx), token.substring(idx + 1)));
        }
        return result;
    }
}
