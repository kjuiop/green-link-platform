package com.greenlink.admin.adapter.out.persistence.taxinvoice;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.taxinvoice.model.TaxInvoice;
import com.greenlink.core.domain.taxinvoice.model.TaxInvoiceStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tax_invoices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tax_invoices_point_tx", columnNames = "point_transaction_id"),
                @UniqueConstraint(name = "uk_tax_invoices_mgt_num", columnNames = "mgt_num")
        })
public class TaxInvoiceJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_transaction_id", nullable = false)
    private Long pointTransactionId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaxInvoiceStatus status;

    @Column(name = "supply_amount", nullable = false)
    private long supplyAmount;

    @Column(name = "tax_amount", nullable = false)
    private long taxAmount;

    @Column(nullable = false)
    private int attempt;

    @Column(name = "mgt_num", nullable = false, length = 50)
    private String mgtNum;

    @Column(name = "doc_key", length = 100)
    private String docKey;

    @Column(name = "fail_reason", length = 400)
    private String failReason;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    protected TaxInvoiceJpaEntity() {}

    public static TaxInvoiceJpaEntity from(TaxInvoice t) {
        TaxInvoiceJpaEntity e = new TaxInvoiceJpaEntity();
        e.id = t.getId();
        e.pointTransactionId = t.getPointTransactionId();
        e.companyId = t.getCompanyId();
        e.status = t.getStatus();
        e.supplyAmount = t.getSupplyAmount();
        e.taxAmount = t.getTaxAmount();
        e.attempt = t.getAttempt();
        e.mgtNum = t.getMgtNum();
        e.docKey = t.getDocKey();
        e.failReason = t.getFailReason();
        e.recipientEmail = t.getRecipientEmail();
        e.requestedBy = t.getRequestedBy();
        e.requestedAt = t.getRequestedAt();
        e.issuedAt = t.getIssuedAt();
        return e;
    }

    public TaxInvoice toDomain() {
        return TaxInvoice.of(id, pointTransactionId, companyId, status, supplyAmount, taxAmount, attempt, mgtNum,
                docKey, failReason, recipientEmail, requestedBy, requestedAt, issuedAt);
    }
}
