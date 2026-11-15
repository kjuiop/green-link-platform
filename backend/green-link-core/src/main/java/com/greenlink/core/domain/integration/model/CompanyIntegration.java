package com.greenlink.core.domain.integration.model;

public class CompanyIntegration {

    private Long id;
    private Long companyId;
    private String provider;
    private String apiKey;
    private String senderNumber;
    private boolean alimtalkEnabled = false;
    private String alimtalkTemplateFuneral;
    private String alimtalkTemplateWedding;
    private String alimtalkTemplateEtc;

    private CompanyIntegration() {}

    public static CompanyIntegration create(Long companyId, String provider, String apiKey, String senderNumber) {
        CompanyIntegration ci = new CompanyIntegration();
        ci.companyId = companyId;
        ci.provider = provider;
        ci.apiKey = apiKey;
        ci.senderNumber = senderNumber;
        return ci;
    }

    public static CompanyIntegration of(Long id, Long companyId, String provider, String apiKey, String senderNumber,
                                        boolean alimtalkEnabled, String alimtalkTemplateFuneral,
                                        String alimtalkTemplateWedding, String alimtalkTemplateEtc) {
        CompanyIntegration ci = new CompanyIntegration();
        ci.id = id;
        ci.companyId = companyId;
        ci.provider = provider;
        ci.apiKey = apiKey;
        ci.senderNumber = senderNumber;
        ci.alimtalkEnabled = alimtalkEnabled;
        ci.alimtalkTemplateFuneral = alimtalkTemplateFuneral;
        ci.alimtalkTemplateWedding = alimtalkTemplateWedding;
        ci.alimtalkTemplateEtc = alimtalkTemplateEtc;
        return ci;
    }

    public void update(String provider, String apiKey, String senderNumber) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.senderNumber = senderNumber;
    }

    public void updateAlimtalk(boolean enabled, String templateFuneral, String templateWedding, String templateEtc) {
        this.alimtalkEnabled = enabled;
        this.alimtalkTemplateFuneral = templateFuneral;
        this.alimtalkTemplateWedding = templateWedding;
        this.alimtalkTemplateEtc = templateEtc;
    }

    public String resolveAlimtalkTemplate(String eventType) {
        if ("장례식".equals(eventType)) return alimtalkTemplateFuneral;
        if ("결혼식".equals(eventType)) return alimtalkTemplateWedding;
        return alimtalkTemplateEtc;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getProvider() { return provider; }
    public String getApiKey() { return apiKey; }
    public String getSenderNumber() { return senderNumber; }
    public boolean isAlimtalkEnabled() { return alimtalkEnabled; }
    public String getAlimtalkTemplateFuneral() { return alimtalkTemplateFuneral; }
    public String getAlimtalkTemplateWedding() { return alimtalkTemplateWedding; }
    public String getAlimtalkTemplateEtc() { return alimtalkTemplateEtc; }
}
