package com.greenlink.core.application.smssettings.service;

import com.greenlink.core.application.smssettings.dto.SaveSmsSettingsCommand;
import com.greenlink.core.application.smssettings.dto.SmsSettingsResult;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsSettingsServiceTest {

    @Mock SmsSettingsRepository repository;

    private SmsSettings barobill() {
        return SmsSettings.of(1L, "02-000-0000", "온우림", "f", "w", "o",
                "BAROBILL", "ABCDEFGH-1234", "baro_id", "1111111111");
    }

    @Test
    void 바로빌_연동인증키는_조회_시_마스킹한다() {
        given(repository.get()).willReturn(barobill());

        SmsSettingsResult result = new SmsSettingsService(repository).get();

        assertThat(result.apiKey()).isEqualTo("****1234");
        assertThat(result.corpNum()).isEqualTo("1111111111");
    }

    @Test
    void 알리고_키는_기존대로_그대로_반환한다() {
        given(repository.get()).willReturn(SmsSettings.of(1L, "", "온우림", "f", "w", "o",
                "ALIGO", "aligo-key", "aligo_id", null));

        assertThat(new SmsSettingsService(repository).get().apiKey()).isEqualTo("aligo-key");
    }

    @Test
    void 마스킹된_키가_다시_저장되면_기존_키를_유지한다() {
        SmsSettings existing = barobill();
        given(repository.get()).willReturn(existing);
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        new SmsSettingsService(repository).save(new SaveSmsSettingsCommand(
                "02-000-0000", "온우림", "f", "w", "o", "BAROBILL", "****1234", "baro_id", "1111111111"));

        ArgumentCaptor<SmsSettings> saved = ArgumentCaptor.forClass(SmsSettings.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getApiKey()).isEqualTo("ABCDEFGH-1234");
    }

    @Test
    void 새_키를_입력하면_교체한다() {
        given(repository.get()).willReturn(barobill());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        new SmsSettingsService(repository).save(new SaveSmsSettingsCommand(
                "02-000-0000", "온우림", "f", "w", "o", "BAROBILL", "NEW-KEY", "baro_id", "1111111111"));

        ArgumentCaptor<SmsSettings> saved = ArgumentCaptor.forClass(SmsSettings.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getApiKey()).isEqualTo("NEW-KEY");
    }

    @Test
    void 제공사가_바뀌면_마스킹된_키로_기존_키를_이어받지_않는다() {
        given(repository.get()).willReturn(barobill());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        new SmsSettingsService(repository).save(new SaveSmsSettingsCommand(
                "02-000-0000", "온우림", "f", "w", "o", "ALIGO", "****1234", "aligo_id", null));

        ArgumentCaptor<SmsSettings> saved = ArgumentCaptor.forClass(SmsSettings.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getApiKey()).isNull();
    }

    @Test
    void 사업자번호의_하이픈은_제거해_저장한다() {
        given(repository.get()).willReturn(barobill());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        new SmsSettingsService(repository).save(new SaveSmsSettingsCommand(
                "02-000-0000", "온우림", "f", "w", "o", "BAROBILL", "NEW-KEY", "baro_id", "476-86-02607"));

        ArgumentCaptor<SmsSettings> saved = ArgumentCaptor.forClass(SmsSettings.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getCorpNum()).isEqualTo("4768602607");
    }
}
