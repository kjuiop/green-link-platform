package com.greenlink.core.application.sms.service;

import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsClientFactory;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsServiceNotifyTest {

    @Mock OrderRepository orderRepository;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock SmsClientFactory smsClientFactory;
    @Mock SmsSettingsRepository smsSettingsRepository;
    @Mock CompanyIntegrationRepository companyIntegrationRepository;
    @Mock CompanyRepository companyRepository;
    @Mock SmsClient smsClient;
    @Mock SmsSettings settings;
    @Mock Order order;

    SmsService smsService;

    @BeforeEach
    void setUp() throws Exception {
        smsService = new SmsService(orderRepository, smsMessageRepository, smsClientFactory,
                smsSettingsRepository, companyIntegrationRepository, companyRepository);
        Field f = SmsService.class.getDeclaredField("portalBaseUrl");
        f.setAccessible(true);
        f.set(smsService, "https://dev.onurim.com");

        given(orderRepository.findById(52L)).willReturn(Optional.of(order));
        given(smsSettingsRepository.get()).willReturn(settings);
        given(smsClientFactory.getByProvider(settings.getProvider())).willReturn(smsClient);
        given(order.getId()).willReturn(52L);
        given(order.getTotalAmount()).willReturn(35_000L);
    }

    @Test
    void 알림_문자의_링크는_주문_상세_팝업_경로이다() {
        smsService.notify(52L, "01031820825", "테스트수주사A", "테스트발주사A", null);

        assertThat(savedMessage().getMessage())
                .contains("https://dev.onurim.com/orders/52/popup")
                .doesNotContain("/vendor/orders/");
    }

    @Test
    void 알림_문자의_발주금액은_추가비용을_포함한_합계이다() {
        smsService.notify(52L, "01031820825", "테스트수주사A", "테스트발주사A", null);

        assertThat(savedMessage().getMessage())
                .contains("발주금액: 35,000원")
                .doesNotContain("30,000원");
    }

    private SmsMessage savedMessage() {
        ArgumentCaptor<SmsMessage> captor = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(captor.capture());
        return captor.getValue();
    }
}
