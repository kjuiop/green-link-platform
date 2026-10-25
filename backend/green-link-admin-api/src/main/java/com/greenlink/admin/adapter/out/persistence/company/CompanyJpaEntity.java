package com.greenlink.admin.adapter.out.persistence.company;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
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

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean displayYn = true;

    protected CompanyJpaEntity() {}

    public static CompanyJpaEntity from(Company company) {
        CompanyJpaEntity e = new CompanyJpaEntity();
        e.id = company.getId();
        String bn = company.getBusinessNo();
        e.businessNo = (bn != null && !bn.isBlank()) ? bn : null;
        e.companyName = company.getCompanyName();
        e.ceoName = company.getCeoName();
        e.phone = company.getPhone();
        e.address = company.getAddress();
        e.detailAddress = company.getDetailAddress();
        e.type = company.getType();
        e.status = company.getStatus();
        e.displayYn = company.isDisplayYn();
        return e;
    }

    public Company toDomain() {
        return Company.of(id, businessNo, companyName, ceoName, phone, address, detailAddress, type, status, displayYn);
    }
}
