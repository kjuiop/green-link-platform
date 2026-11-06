package com.greenlink.admin.adapter.out.persistence.company;

import com.greenlink.core.domain.company.model.CompanyCapability;
import jakarta.persistence.*;

@Entity
@Table(name = "company_capabilities")
public class CompanyCapabilityJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CompanyCapability category;

    protected CompanyCapabilityJpaEntity() {}

    public static CompanyCapabilityJpaEntity from(Long companyId, CompanyCapability category) {
        CompanyCapabilityJpaEntity e = new CompanyCapabilityJpaEntity();
        e.companyId = companyId;
        e.category = category;
        return e;
    }

    public CompanyCapability toDomain() {
        return category;
    }
}
