package com.greenlink.core.domain.company.model;

import java.util.List;

public class Company {

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
        return c;
    }

    public static Company of(Long id, String businessNo, String companyName,
                              String ceoName, String phone, String faxNumber,
                              String address, String detailAddress,
                              CompanyType type, CompanyStatus status, boolean displayYn,
                              String businessRegUrl, String bankBookUrl) {
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
        return c;
    }

    public void expandType(CompanyType requestedType) {
        if (this.type == requestedType) return;
        this.type = CompanyType.BOTH;
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
    public List<CompanyRegion> getRegions() { return regions; }
    public List<CompanyCapability> getCapabilities() { return capabilities; }
}
