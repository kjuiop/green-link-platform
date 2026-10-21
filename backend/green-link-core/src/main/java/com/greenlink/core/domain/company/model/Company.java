package com.greenlink.core.domain.company.model;

public class Company {

    private Long id;
    private String businessNo;
    private String companyName;
    private String ceoName;
    private String phone;
    private String address;
    private CompanyType type;
    private CompanyStatus status;

    private Company() {}

    public static Company create(String businessNo, String companyName,
                                  String ceoName, String phone,
                                  String address, CompanyType type) {
        Company c = new Company();
        c.businessNo = businessNo;
        c.companyName = companyName;
        c.ceoName = ceoName;
        c.phone = phone;
        c.address = address;
        c.type = type;
        c.status = CompanyStatus.ACTIVE;
        return c;
    }

    public static Company of(Long id, String businessNo, String companyName,
                              String ceoName, String phone, String address,
                              CompanyType type, CompanyStatus status) {
        Company c = new Company();
        c.id = id;
        c.businessNo = businessNo;
        c.companyName = companyName;
        c.ceoName = ceoName;
        c.phone = phone;
        c.address = address;
        c.type = type;
        c.status = status;
        return c;
    }

    public void expandType(CompanyType requestedType) {
        if (this.type == requestedType) return;
        this.type = CompanyType.BOTH;
    }

    public void updateInfo(String companyName, String businessNo, String address) {
        this.companyName = companyName;
        this.businessNo = businessNo;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getBusinessNo() { return businessNo; }
    public String getCompanyName() { return companyName; }
    public String getCeoName() { return ceoName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public CompanyType getType() { return type; }
    public CompanyStatus getStatus() { return status; }
}
