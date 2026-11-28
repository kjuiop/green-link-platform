package com.greenlink.core.application.sms.service;

import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClientFactory;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SmsServicePreparationFailureTest {

    @Mock OrderRepository orderRepository;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock SmsClientFactory smsClientFactory;
    @Mock CompanyIntegrationRepository companyIntegrationRepository;
    @Mock CompanyRepository companyRepository;
    @Mock SmsSettingsRepository smsSettingsRepository;
    @Mock Order order;
    @Mock SmsSettings settings;

    SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(orderRepository, smsMessageRepository, smsClientFactory,
                smsSettingsRepository, companyIntegrationRepository, companyRepository);
        given(smsMessageRepository.findAllByOrderId(1L)).willReturn(List.of());
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(order.getId()).willReturn(1L);
        given(smsSettingsRepository.get()).willReturn(settings);
        given(settings.resolveTemplate(any())).willThrow(new IllegalStateException("템플릿 오류"));
    }

    @Test
    void 발송_준비_단계에서_실패하면_수신자별로_FAIL과_사유를_기록하고_발송하지_않는다() {
        given(order.isSendPhotoToOrderer()).willReturn(true);
        given(order.getOrdererPhone()).willReturn("01011112222");
        given(order.isSendPhotoToRecipient()).willReturn(true);
        given(order.getRecipientPhone()).willReturn("01033334444");

        smsService.send(1L, null);

        ArgumentCaptor<SmsMessage> captor = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(SmsMessage::getReceiver)
                .containsExactly("01011112222", "01033334444");
        assertThat(captor.getAllValues()).allSatisfy(m -> {
            assertThat(m.getStatus()).isEqualTo(SmsStatus.FAIL);
            assertThat(m.getFailReason()).startsWith("발송 준비 실패: 템플릿 오류");
        });
        verifyNoInteractions(smsClientFactory);
    }

    @Test
    void 발송_대상이_없으면_준비_실패여도_행을_만들지_않는다() {
        given(order.isSendPhotoToOrderer()).willReturn(false);
        given(order.isSendPhotoToRecipient()).willReturn(false);

        smsService.send(1L, null);

        verify(smsMessageRepository, never()).save(any());
    }
}
