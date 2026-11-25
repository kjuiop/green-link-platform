package com.greenlink.core.domain.company.model;

import java.util.List;

public class Company {

    public static final long DEFAULT_VENDOR_FEE = 3000L;
    public static final long MAX_VENDOR_FEE = 10_000_000L;

    private Long id;
    private String businessNo;
    private String companyName;
    private String ceoName;
    private String phone;
    private String faxNumber;
    private String address;
    private String detailAddress;
    private CompanyType type;
    private CompanyStatus status;
    private boolean displayYn;
    private String businessRegUrl;
    private String bankBookUrl;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private BusinessStatus businessStatus = BusinessStatus.OPEN;
    private long vendorFee = DEFAULT_VENDOR_FEE;
    private OrderingFeeDiscountType orderingFeeDiscountType = OrderingFeeDiscountType.NONE;
    private long orderingFeeDiscountValue = 0L;
    private boolean faxEnabled;
    private String taxInvoiceEmail;
    private boolean onurimEnabled = true;
    private boolean siteEnabled;
    private List<CompanyRegion> regions = List.of();
    private List<CompanyCapability> capabilities = List.of();

    private Company() {}

    public static Company create(String businessNo, String companyName,
                                  String ceoName, String phone, String faxNumber,
                                  String address, String detailAddress, CompanyType type) {
        Company c = new Company();
        c.businessNo = businessNo;
        c.companyName = companyName;
        c.ceoName = ceoName;
        c.phone = phone;
        c.faxNumber = faxNumber;
        c.address = address;
        c.detailAddress = detailAddress;
        c.type = type;
        c.status = CompanyStatus.ACTIVE;
        c.displayYn = true;
        c.businessStatus = BusinessStatus.OPEN;
        return c;
    }

    public static Company createForShop(String businessNo, String companyName, String ceoName, String phone) {
        Company c = create(businessNo, companyName, ceoName, phone, null, "", "", null);
        c.onurimEnabled = false;
        c.siteEnabled = true;
        return c;
    }

    public static Company of(Long id, String businessNo, String companyName,
                              String ceoName, String phone, String faxNumber,
                              String address, String detailAddress,
                              CompanyType type, CompanyStatus status, boolean displayYn,
                              String businessRegUrl, String bankBookUrl,
                              String bankName, String accountNumber, String accountHolder,
                              BusinessStatus businessStatus) {
        return of(id, businessNo, companyName, ceoName, phone, faxNumber, address, detailAddress,
                type, status, displayYn, businessRegUrl, bankBookUrl, bankName, accountNumber,
                accountHolder, businessStatus, DEFAULT_VENDOR_FEE);
    }

    public static Company of(Long id, String businessNo, String companyName,
                              String ceoName, String phone, String faxNumber,
                              String address, String detailAddress,
                              CompanyType type, CompanyStatus status, boolean displayYn,
                              String businessRegUrl, String bankBookUrl,
                              String bankName, String accountNumber, String accountHolder,
                              BusinessStatus businessStatus, long vendorFee) {
        Company c = new Company();
        c.id = id;
        c.businessNo = businessNo;
        c.companyName = companyName;
        c.ceoName = ceoName;
        c.phone = phone;
        c.faxNumber = faxNumber;
        c.address = address;
        c.detailAddress = detailAddress;
        c.type = type;
        c.status = status;
        c.displayYn = displayYn;
        c.businessRegUrl = businessRegUrl;
        c.bankBookUrl = bankBookUrl;
        c.bankName = bankName;
        c.accountNumber = accountNumber;
        c.accountHolder = accountHolder;
        c.businessStatus = businessStatus != null ? businessStatus : BusinessStatus.OPEN;
        c.vendorFee = vendorFee;
        return c;
    }

    public void expandType(CompanyType requestedType) {
        if (this.type == requestedType) return;
        this.type = this.type == null ? requestedType : CompanyType.BOTH;
    }

    public void updateType(CompanyType newType) {
        this.type = newType;
    }

    public void updateInfo(String companyName, String businessNo, String faxNumber, String address, String detailAddress) {
        this.companyName = companyName;
        this.businessNo = businessNo;
        this.faxNumber = faxNumber;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updateDocumentUrl(String docType, String url) {
        if ("businessReg".equals(docType)) this.businessRegUrl = url;
        else if ("bankBook".equals(docType)) this.bankBookUrl = url;
    }

    public void updateBankAccount(String bankName, String accountNumber, String accountHolder) {
        this.bankName = (bankName != null && !bankName.isBlank()) ? bankName : null;
        this.accountNumber = (accountNumber != null && !accountNumber.isBlank()) ? accountNumber : null;
        this.accountHolder = (accountHolder != null && !accountHolder.isBlank()) ? accountHolder : null;
    }

    public void updateBusinessStatus(BusinessStatus status) {
        this.businessStatus = status;
    }

    public void changeVendorFee(long fee) {
        if (fee < 0 || fee > MAX_VENDOR_FEE) {
            throw new IllegalArgumentException("수수료는 0원 이상 " + MAX_VENDOR_FEE + "원 이하여야 합니다.");
        }
        this.vendorFee = fee;
    }

    public void changeOrderingFeeDiscount(OrderingFeeDiscountType type, long value) {
        if (value < 0) throw new IllegalArgumentException("감면 값은 0 이상이어야 합니다.");
        if (type == OrderingFeeDiscountType.PERCENT && value > 100)
            throw new IllegalArgumentException("퍼센트는 100 이하여야 합니다.");
        this.orderingFeeDiscountType = type != null ? type : OrderingFeeDiscountType.NONE;
        this.orderingFeeDiscountValue = value;
    }

    public void restoreOrderingFeeDiscount(OrderingFeeDiscountType type, long value) {
        this.orderingFeeDiscountType = type != null ? type : OrderingFeeDiscountType.NONE;
        this.orderingFeeDiscountValue = value;
    }

    public long calcOrderingDiscount(long totalAmount) {
        return switch (orderingFeeDiscountType) {
            case NONE -> 0L;
            case FIXED -> Math.min(orderingFeeDiscountValue, totalAmount);
            case PERCENT -> Math.round(totalAmount * orderingFeeDiscountValue / 100.0);
        };
    }

    public void restoreServiceFlags(boolean onurimEnabled, boolean siteEnabled) {
        this.onurimEnabled = onurimEnabled;
        this.siteEnabled = siteEnabled;
    }

    public void enableSite() {
        this.siteEnabled = true;
    }

    public boolean isOnurimCompany() {
        return onurimEnabled && type != null;
    }

    public void restoreFaxEnabled(boolean faxEnabled) {
        this.faxEnabled = faxEnabled;
    }

    public void restoreTaxInvoiceEmail(String taxInvoiceEmail) {
        this.taxInvoiceEmail = taxInvoiceEmail;
    }

    public void changeTaxInvoiceEmail(String taxInvoiceEmail) {
        this.taxInvoiceEmail = taxInvoiceEmail == null || taxInvoiceEmail.isBlank() ? null : taxInvoiceEmail.trim();
    }

    public void changeFaxSetting(boolean faxEnabled, String faxNumber) {
        String digits = faxNumber == null ? "" : faxNumber.replaceAll("[^0-9]", "");
        if (faxEnabled && digits.isEmpty()) {
            throw new IllegalArgumentException("팩스 연동 시 팩스번호가 필요합니다.");
        }
        this.faxEnabled = faxEnabled;
        this.faxNumber = digits.isEmpty() ? null : digits;
    }

    public boolean canReceiveFax() {
        return faxEnabled && faxNumber != null && !faxNumber.replaceAll("[^0-9]", "").isEmpty();
    }

    public Long getId() { return id; }
    public String getBusinessNo() { return businessNo; }
    public String getCompanyName() { return companyName; }
    public String getCeoName() { return ceoName; }
    public String getPhone() { return phone; }
    public String getFaxNumber() { return faxNumber; }
    public String getAddress() { return address; }
    public String getDetailAddress() { return detailAddress; }
    public CompanyType getType() { return type; }
    public CompanyStatus getStatus() { return status; }
    public boolean isDisplayYn() { return displayYn; }
    public String getBusinessRegUrl() { return businessRegUrl; }
    public String getBankBookUrl() { return bankBookUrl; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public BusinessStatus getBusinessStatus() { return businessStatus; }
    public long getVendorFee() { return vendorFee; }
    public OrderingFeeDiscountType getOrderingFeeDiscountType() { return orderingFeeDiscountType; }
    public long getOrderingFeeDiscountValue() { return orderingFeeDiscountValue; }
    public boolean isFaxEnabled() { return faxEnabled; }
    public boolean isOnurimEnabled() { return onurimEnabled; }
    public boolean isSiteEnabled() { return siteEnabled; }
    public String getTaxInvoiceEmail() { return taxInvoiceEmail; }
    public List<CompanyRegion> getRegions() { return regions; }
    public List<CompanyCapability> getCapabilities() { return capabilities; }
}
