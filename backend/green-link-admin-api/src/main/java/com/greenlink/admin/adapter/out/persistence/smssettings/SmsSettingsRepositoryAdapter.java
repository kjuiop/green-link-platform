package com.greenlink.admin.adapter.out.persistence.smssettings;

import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import org.springframework.stereotype.Component;

@Component
public class SmsSettingsRepositoryAdapter implements SmsSettingsRepository {

    private static final String DEFAULT_TEMPLATE_FUNERAL =
            "[{업체명}] {주문자명}님, 삼가 고인의 명복을 빕니다.\n주문하신 근조화환이 {장소}에 배송 완료되었습니다.";
    private static final String DEFAULT_TEMPLATE_WEDDING =
            "[{업체명}] {주문자명}님, 주문하신 축하화환이 {장소}에 배송 완료되었습니다.\n결혼을 진심으로 축하드립니다.";
    private static final String DEFAULT_TEMPLATE_OTHERS =
            "[{업체명}] {주문자명}님, 주문하신 화환이 {장소}에 배송 완료되었습니다.";

    private final SmsSettingsJpaRepository jpaRepository;

    public SmsSettingsRepositoryAdapter(SmsSettingsJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SmsSettings get() {
        return jpaRepository.findAll().stream()
                .findFirst()
                .map(SmsSettingsJpaEntity::toDomain)
                .orElseGet(() -> SmsSettings.of(null, "", "온우림",
                        DEFAULT_TEMPLATE_FUNERAL, DEFAULT_TEMPLATE_WEDDING, DEFAULT_TEMPLATE_OTHERS));
    }

    @Override
    public SmsSettings save(SmsSettings settings) {
        SmsSettingsJpaEntity entity = jpaRepository.findAll().stream()
                .findFirst()
                .orElseGet(SmsSettingsJpaEntity::new);
        entity.update(settings.getSenderNumber(), settings.getSenderName(),
                settings.getTemplateFuneral(), settings.getTemplateWedding(), settings.getTemplateOthers());
        return jpaRepository.save(entity).toDomain();
    }
}
