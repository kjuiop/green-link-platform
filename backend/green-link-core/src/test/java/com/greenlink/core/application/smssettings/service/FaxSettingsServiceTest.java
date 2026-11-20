package com.greenlink.core.application.smssettings.service;

import com.greenlink.core.application.smssettings.dto.FaxSettingsResult;
import com.greenlink.core.application.smssettings.dto.SaveFaxSettingsCommand;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.fax.FaxErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FaxSettingsServiceTest {

    @Mock SmsSettingsRepository repository;

    private SmsSettings withFax() {
        SmsSettings s = SmsSettings.of(1L, "", "온우림", "f", "w", "o", "BAROBILL", "key", "id", "1111111111");
        s.withFax("0212345678", "https://ws.baroservice.com/FAX.asmx", "ftp.example.com", 21, "ftpuser", "secret-pass", "/fax");
        return s;
    }

    @Test
    void FTP_비밀번호는_조회_시_마스킹한다() {
        given(repository.get()).willReturn(withFax());

        FaxSettingsResult result = new FaxSettingsService(repository).get();

        assertThat(result.ftpPassword()).isEqualTo("****pass");
        assertThat(result.ftpHost()).isEqualTo("ftp.example.com");
    }

    @Test
    void 마스킹된_비밀번호가_다시_저장되면_기존_값을_유지한다() {
        SmsSettings existing = withFax();
        given(repository.get()).willReturn(existing);
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        new FaxSettingsService(repository).save(new SaveFaxSettingsCommand(
                "02-123-4567", "https://ws.baroservice.com/FAX.asmx", "ftp.example.com", 21, "ftpuser", "****pass", "/fax"));

        assertThat(existing.getFtpPassword()).isEqualTo("secret-pass");
        assertThat(existing.getFaxSenderNumber()).isEqualTo("021234567");
    }

    @Test
    void 새_비밀번호는_교체한다() {
        SmsSettings existing = withFax();
        given(repository.get()).willReturn(existing);
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        new FaxSettingsService(repository).save(new SaveFaxSettingsCommand(
                "0212345678", null, "ftp.example.com", 2121, "ftpuser", "new-pass", null));

        assertThat(existing.getFtpPassword()).isEqualTo("new-pass");
        assertThat(existing.getFaxUrl()).isNull();
        assertThat(existing.getFtpPort()).isEqualTo(2121);
    }

    @Test
    void 바로빌_도메인이_아닌_URL은_거부한다() {
        assertThatThrownBy(() -> new FaxSettingsService(repository).save(new SaveFaxSettingsCommand(
                "0212345678", "https://evil.example.com/FAX.asmx", "h", 21, "u", "p", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(FaxErrorCode.FAX_URL_INVALID));
        assertThatThrownBy(() -> new FaxSettingsService(repository).save(new SaveFaxSettingsCommand(
                "0212345678", "http://ws.baroservice.com/FAX.asmx", "h", 21, "u", "p", null)))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void 잘못된_포트는_거부한다() {
        assertThatThrownBy(() -> new FaxSettingsService(repository).save(new SaveFaxSettingsCommand(
                "0212345678", null, "h", 70000, "u", "p", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(FaxErrorCode.FAX_FTP_PORT_INVALID));
    }

    @Test
    void 컬럼_길이를_넘는_값은_거부한다() {
        assertThatThrownBy(() -> new FaxSettingsService(repository).save(new SaveFaxSettingsCommand(
                "0212345678", null, "h".repeat(101), 21, "u", "p", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(FaxErrorCode.FAX_SETTINGS_TOO_LONG));
        verify(repository, never()).save(any());
    }
}
