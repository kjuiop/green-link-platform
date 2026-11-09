package com.greenlink.core.domain.smssettings.model;

public class SmsSettings {

    private Long id;
    private String senderNumber;
    private String senderName;
    private String templateFuneral;
    private String templateWedding;
    private String templateOthers;
    private String provider;
    private String apiKey;
    private String userId;

    private SmsSettings() {}

    public static SmsSettings of(Long id, String senderNumber, String senderName,
                                  String templateFuneral, String templateWedding, String templateOthers) {
        SmsSettings s = new SmsSettings();
        s.id = id;
        s.senderNumber = senderNumber;
        s.senderName = senderName;
        s.templateFuneral = templateFuneral;
        s.templateWedding = templateWedding;
        s.templateOthers = templateOthers;
        s.provider = "ALIGO";
        return s;
    }

    public static SmsSettings of(Long id, String senderNumber, String senderName,
                                  String templateFuneral, String templateWedding, String templateOthers,
                                  String provider, String apiKey, String userId) {
        SmsSettings s = new SmsSettings();
        s.id = id;
        s.senderNumber = senderNumber;
        s.senderName = senderName;
        s.templateFuneral = templateFuneral;
        s.templateWedding = templateWedding;
        s.templateOthers = templateOthers;
        s.provider = provider != null ? provider : "ALIGO";
        s.apiKey = apiKey;
        s.userId = userId;
        return s;
    }

    public void update(String senderNumber, String senderName,
                       String templateFuneral, String templateWedding, String templateOthers,
                       String provider, String apiKey, String userId) {
        this.senderNumber = senderNumber;
        this.senderName = senderName;
        this.templateFuneral = templateFuneral;
        this.templateWedding = templateWedding;
        this.templateOthers = templateOthers;
        this.provider = provider != null ? provider : "ALIGO";
        this.apiKey = apiKey;
        this.userId = userId;
    }

    public String resolveTemplate(String eventType) {
        if ("장례식".equals(eventType)) return templateFuneral;
        if ("결혼식".equals(eventType)) return templateWedding;
        return templateOthers;
    }

    public Long getId() { return id; }
    public String getSenderNumber() { return senderNumber; }
    public String getSenderName() { return senderName; }
    public String getTemplateFuneral() { return templateFuneral; }
    public String getTemplateWedding() { return templateWedding; }
    public String getTemplateOthers() { return templateOthers; }
    public String getProvider() { return provider != null ? provider : "ALIGO"; }
    public String getApiKey() { return apiKey; }
    public String getUserId() { return userId; }
}
