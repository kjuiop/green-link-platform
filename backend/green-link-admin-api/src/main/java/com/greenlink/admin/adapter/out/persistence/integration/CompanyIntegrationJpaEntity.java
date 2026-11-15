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

    @Column(nullable = false)
    private boolean alimtalkEnabled = false;

    @Column(length = 100)
    private String alimtalkTemplateFuneral;

    @Column(length = 100)
    private String alimtalkTemplateWedding;

    @Column(length = 100)
    private String alimtalkTemplateEtc;

    @Column(length = 100)
    private String alimtalkSenderKey;

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

    void updateAlimtalk(boolean enabled, String templateFuneral, String templateWedding,
                        String templateEtc, String senderKey) {
        this.alimtalkEnabled = enabled;
        this.alimtalkTemplateFuneral = templateFuneral;
        this.alimtalkTemplateWedding = templateWedding;
        this.alimtalkTemplateEtc = templateEtc;
        this.alimtalkSenderKey = senderKey;
    }

    CompanyIntegration toDomain() {
        return CompanyIntegration.of(id, companyId, provider, apiKey, senderNumber,
                alimtalkEnabled, alimtalkTemplateFuneral, alimtalkTemplateWedding,
                alimtalkTemplateEtc, alimtalkSenderKey);
    }
}
