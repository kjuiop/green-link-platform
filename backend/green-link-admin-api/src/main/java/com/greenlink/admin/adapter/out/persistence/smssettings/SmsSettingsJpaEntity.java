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

    protected SmsSettingsJpaEntity() {}

    public void update(String senderNumber, String senderName,
                       String templateFuneral, String templateWedding, String templateOthers) {
        this.senderNumber = senderNumber;
        this.senderName = senderName;
        this.templateFuneral = templateFuneral;
        this.templateWedding = templateWedding;
        this.templateOthers = templateOthers;
    }

    public SmsSettings toDomain() {
        return SmsSettings.of(id, senderNumber, senderName, templateFuneral, templateWedding, templateOthers);
    }
}
