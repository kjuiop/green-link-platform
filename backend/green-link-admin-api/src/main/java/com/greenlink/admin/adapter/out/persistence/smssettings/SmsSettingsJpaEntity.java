package com.greenlink.admin.adapter.out.persistence.smssettings;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import jakarta.persistence.*;

@Entity
@Table(name = "sms_settings")
public class SmsSettingsJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String senderNumber;

    @Column(length = 100)
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String templateFuneral;

    @Column(columnDefinition = "TEXT")
    private String templateWedding;

    @Column(columnDefinition = "TEXT")
    private String templateOthers;

    @Column(length = 50)
    private String provider;

    @Column(length = 200)
    private String apiKey;

    @Column(length = 100)
    private String userId;

    @Column(length = 10)
    private String corpNum;

    @Column(length = 20)
    private String faxSenderNumber;

    @Column(length = 200)
    private String faxUrl;

    @Column(length = 100)
    private String ftpHost;

    private Integer ftpPort;

    @Column(length = 100)
    private String ftpUsername;

    @Column(length = 200)
    private String ftpPassword;

    @Column(length = 200)
    private String ftpDirectory;

    protected SmsSettingsJpaEntity() {}

    public void update(String senderNumber, String senderName,
                       String templateFuneral, String templateWedding, String templateOthers,
                       String provider, String apiKey, String userId, String corpNum) {
        this.senderNumber = senderNumber;
        this.senderName = senderName;
        this.templateFuneral = templateFuneral;
        this.templateWedding = templateWedding;
        this.templateOthers = templateOthers;
        this.provider = provider != null ? provider : "ALIGO";
        this.apiKey = apiKey;
        this.userId = userId;
        this.corpNum = corpNum;
    }

    public void updateFax(String faxSenderNumber, String faxUrl, String ftpHost, Integer ftpPort,
                          String ftpUsername, String ftpPassword, String ftpDirectory) {
        this.faxSenderNumber = faxSenderNumber;
        this.faxUrl = faxUrl;
        this.ftpHost = ftpHost;
        this.ftpPort = ftpPort;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.ftpDirectory = ftpDirectory;
    }

    public SmsSettings toDomain() {
        SmsSettings s = SmsSettings.of(id, senderNumber, senderName, templateFuneral, templateWedding, templateOthers,
                provider, apiKey, userId, corpNum);
        s.withFax(faxSenderNumber, faxUrl, ftpHost, ftpPort, ftpUsername, ftpPassword, ftpDirectory);
        return s;
    }
}
