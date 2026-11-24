package com.greenlink.admin.adapter.out.persistence.company;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.model.OrderingFeeDiscountType;
import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class CompanyJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String businessNo;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(length = 50)
    private String ceoName;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String faxNumber;

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CompanyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean displayYn = true;

    @Column(length = 500)
    private String businessRegUrl;

    @Column(length = 500)
    private String bankBookUrl;

    @Column(length = 50)
    private String bankName;

    @Column(length = 50)
    private String accountNumber;

    @Column(length = 50)
    private String accountHolder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'OPEN'")
    private BusinessStatus businessStatus = BusinessStatus.OPEN;

    @Column(name = "vendor_fee", nullable = false, columnDefinition = "BIGINT DEFAULT 3000")
    private long vendorFee = Company.DEFAULT_VENDOR_FEE;

    @Enumerated(EnumType.STRING)
    @Column(name = "ordering_fee_discount_type", nullable = false, length = 20,
            columnDefinition = "ENUM('NONE','PERCENT','FIXED') DEFAULT 'NONE'")
    private OrderingFeeDiscountType orderingFeeDiscountType = OrderingFeeDiscountType.NONE;

    @Column(name = "ordering_fee_discount_value", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long orderingFeeDiscountValue = 0L;

    @Column(name = "fax_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean faxEnabled;

    @Column(name = "tax_invoice_email", length = 255)
    private String taxInvoiceEmail;

    @Column(name = "onurim_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean onurimEnabled = true;

    @Column(name = "site_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean siteEnabled;

    protected CompanyJpaEntity() {}

    public static CompanyJpaEntity from(Company company) {
        CompanyJpaEntity e = new CompanyJpaEntity();
        e.id = company.getId();
        String bn = company.getBusinessNo();
        e.businessNo = (bn != null && !bn.isBlank()) ? bn : null;
        e.companyName = company.getCompanyName();
        e.ceoName = company.getCeoName();
        e.phone = company.getPhone();
        e.faxNumber = company.getFaxNumber();
        e.address = company.getAddress();
        e.detailAddress = company.getDetailAddress();
        e.type = company.getType();
        e.status = company.getStatus();
        e.displayYn = company.isDisplayYn();
        e.businessRegUrl = company.getBusinessRegUrl();
        e.bankBookUrl = company.getBankBookUrl();
        e.bankName = company.getBankName();
        e.accountNumber = company.getAccountNumber();
        e.accountHolder = company.getAccountHolder();
        e.businessStatus = company.getBusinessStatus() != null ? company.getBusinessStatus() : BusinessStatus.OPEN;
        e.vendorFee = company.getVendorFee();
        e.orderingFeeDiscountType = company.getOrderingFeeDiscountType();
        e.orderingFeeDiscountValue = company.getOrderingFeeDiscountValue();
        e.faxEnabled = company.isFaxEnabled();
        e.taxInvoiceEmail = company.getTaxInvoiceEmail();
        e.onurimEnabled = company.isOnurimEnabled();
        e.siteEnabled = company.isSiteEnabled();
        return e;
    }

    public Company toDomain() {
        Company company = Company.of(id, businessNo, companyName, ceoName, phone, faxNumber,
                address, detailAddress, type, status, displayYn, businessRegUrl, bankBookUrl,
                bankName, accountNumber, accountHolder, businessStatus, vendorFee);
        company.restoreOrderingFeeDiscount(orderingFeeDiscountType, orderingFeeDiscountValue);
        company.restoreFaxEnabled(faxEnabled);
        company.restoreTaxInvoiceEmail(taxInvoiceEmail);
        company.restoreServiceFlags(onurimEnabled, siteEnabled);
        return company;
    }
}
