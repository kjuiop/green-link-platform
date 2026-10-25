package com.greenlink.core.domain.company.model;

public class Company {

    private Long id;
    private String businessNo;
    private String companyName;
    private String ceoName;
    private String phone;
    private String address;
    private String detailAddress;
    private CompanyType type;
    private CompanyStatus status;

    private Company() {}

    public static Company create(String businessNo, String companyName,
                                  String ceoName, String phone,
                                  String address, String detailAddress, CompanyType type) {
        Company c = new Company();
        c.businessNo = businessNo;
        c.companyName = companyName;
        c.ceoName = ceoName;
        c.phone = phone;
        c.address = address;
        c.detailAddress = detailAddress;
        c.type = type;
        c.status = CompanyStatus.ACTIVE;
        return c;
    }

    public static Company of(Long id, String businessNo, String companyName,
                              String ceoName, String phone, String address, String detailAddress,
                              CompanyType type, CompanyStatus status) {
        Company c = new Company();
        c.id = id;
        c.businessNo = businessNo;
        c.companyName = companyName;
        c.ceoName = ceoName;
        c.phone = phone;
        c.address = address;
        c.detailAddress = detailAddress;
        c.type = type;
        c.status = status;
        return c;
    }

    public void expandType(CompanyType requestedType) {
        if (this.type == requestedType) return;
        this.type = CompanyType.BOTH;
    }

    public void updateType(CompanyType newType) {
        this.type = newType;
    }

    public void updateInfo(String companyName, String businessNo, String address, String detailAddress) {
        this.companyName = companyName;
        this.businessNo = businessNo;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public Long getId() { return id; }
    public String getBusinessNo() { return businessNo; }
    public String getCompanyName() { return companyName; }
    public String getCeoName() { return ceoName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getDetailAddress() { return detailAddress; }
    public CompanyType getType() { return type; }
    public CompanyStatus getStatus() { return status; }
}
