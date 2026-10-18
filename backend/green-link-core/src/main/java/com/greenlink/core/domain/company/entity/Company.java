package com.greenlink.core.domain.company.entity;

import com.greenlink.core.global.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String businessNo;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(length = 50)
    private String ceoName;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status = CompanyStatus.ACTIVE;

    protected Company() {}

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
        return c;
    }

    public void expandType(CompanyType requestedType) {
        if (this.type == requestedType) return;
        this.type = CompanyType.BOTH;
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
