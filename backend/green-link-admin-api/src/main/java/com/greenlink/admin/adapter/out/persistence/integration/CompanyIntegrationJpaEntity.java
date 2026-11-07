package com.greenlink.admin.adapter.out.persistence.integration;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import jakarta.persistence.*;

@Entity
@Table(name = "company_integrations")
public class CompanyIntegrationJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(length = 200)
    private String apiKey;

    @Column(length = 20)
    private String senderNumber;

    protected CompanyIntegrationJpaEntity() {}

    static CompanyIntegrationJpaEntity createNew(Long companyId, String provider, String apiKey, String senderNumber) {
        CompanyIntegrationJpaEntity e = new CompanyIntegrationJpaEntity();
        e.companyId = companyId;
        e.provider = provider;
        e.apiKey = apiKey;
        e.senderNumber = senderNumber;
        return e;
    }

    void update(String provider, String apiKey, String senderNumber) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.senderNumber = senderNumber;
    }

    CompanyIntegration toDomain() {
        return CompanyIntegration.of(id, companyId, provider, apiKey, senderNumber);
    }
}
