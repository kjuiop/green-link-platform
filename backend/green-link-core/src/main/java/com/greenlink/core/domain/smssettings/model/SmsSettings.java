package com.greenlink.core.domain.smssettings.model;

public class SmsSettings {

    private Long id;
    private String senderNumber;
    private String senderName;
    private String templateFuneral;
    private String templateWedding;
    private String templateOthers;

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
        return s;
    }

    public void update(String senderNumber, String senderName,
                       String templateFuneral, String templateWedding, String templateOthers) {
        this.senderNumber = senderNumber;
        this.senderName = senderName;
        this.templateFuneral = templateFuneral;
        this.templateWedding = templateWedding;
        this.templateOthers = templateOthers;
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
}
