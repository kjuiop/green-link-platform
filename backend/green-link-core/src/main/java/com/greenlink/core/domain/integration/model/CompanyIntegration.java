package com.greenlink.core.domain.integration.model;

public class CompanyIntegration {

    private Long id;
    private Long companyId;
    private String provider;
    private String apiKey;
    private String senderNumber;

    private CompanyIntegration() {}

    public static CompanyIntegration create(Long companyId, String provider, String apiKey, String senderNumber) {
        CompanyIntegration ci = new CompanyIntegration();
        ci.companyId = companyId;
        ci.provider = provider;
        ci.apiKey = apiKey;
        ci.senderNumber = senderNumber;
        return ci;
    }

    public static CompanyIntegration of(Long id, Long companyId, String provider, String apiKey, String senderNumber) {
        CompanyIntegration ci = new CompanyIntegration();
        ci.id = id;
        ci.companyId = companyId;
        ci.provider = provider;
        ci.apiKey = apiKey;
        ci.senderNumber = senderNumber;
        return ci;
    }

    public void update(String provider, String apiKey, String senderNumber) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.senderNumber = senderNumber;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getProvider() { return provider; }
    public String getApiKey() { return apiKey; }
    public String getSenderNumber() { return senderNumber; }
}
