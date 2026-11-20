package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.sms.dto.BarobillQueryResult;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.sms.model.SmsAccount;
import com.greenlink.core.domain.sms.port.BarobillGateway;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BarobillQueryServiceTest {

    @Mock BarobillGateway gateway;
    @Mock SmsSettingsRepository smsSettingsRepository;
    @Mock CompanyIntegrationRepository companyIntegrationRepository;

    private BarobillQueryService service() {
        return new BarobillQueryService(gateway, smsSettingsRepository, companyIntegrationRepository);
    }

    @Test
    void 업체_계정이_없으면_운영자_계정으로_폴백하지_않고_실패한다() {
        given(companyIntegrationRepository.findByCompanyId(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().checkSenderNumber(10L)).isInstanceOf(BusinessException.class);
        verify(gateway, never()).checkSenderNumber(any());
    }

    @Test
    void 업체_계정이_있으면_그_계정으로_조회한다() {
        given(companyIntegrationRepository.findByCompanyId(10L)).willReturn(Optional.of(
                CompanyIntegration.create(10L, "BAROBILL", "KEY", "031-111-1111", "baro_id", "2222222222")));
        given(gateway.checkSenderNumber(new SmsAccount("BAROBILL", "KEY", "031-111-1111", "baro_id", "2222222222")))
                .willReturn(1);

        BarobillQueryResult result = service().checkSenderNumber(10L);

        assertThat(result.code()).isEqualTo(1);
    }

    @Test
    void 업체를_지정하지_않으면_운영자_계정으로_조회한다() {
        given(smsSettingsRepository.get()).willReturn(SmsSettings.of(1L, "02-000-0000", "온우림", "f", "w", "o",
                "BAROBILL", "GLOBAL", "global_id", "1111111111"));
        given(gateway.checkSenderNumber(new SmsAccount("BAROBILL", "GLOBAL", "02-000-0000", "global_id", "1111111111")))
                .willReturn(2);

        assertThat(service().checkSenderNumber(null).message()).contains("등록되지 않은");
    }
}
