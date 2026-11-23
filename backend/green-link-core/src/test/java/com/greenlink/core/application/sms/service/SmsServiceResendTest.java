package com.greenlink.core.application.sms.service;

import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsClientFactory;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsServiceResendTest {

    @Mock OrderRepository orderRepository;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock SmsClient smsClient;
    @Mock SmsClientFactory smsClientFactory;
    @Mock CompanyIntegrationRepository companyIntegrationRepository;
    @Mock CompanyRepository companyRepository;
    @Mock SmsSettingsRepository smsSettingsRepository;
    @Mock Order order;
    @Mock SmsMessage failedMessage;
    @Mock SmsSettings settings;

    SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(orderRepository, smsMessageRepository, smsClientFactory,
                smsSettingsRepository, companyIntegrationRepository, companyRepository);
        given(failedMessage.getStatus()).willReturn(SmsStatus.FAIL);
        given(smsMessageRepository.findAllByOrderId(1L)).willReturn(List.of(failedMessage));
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
    }

    @Test
    void resend_onDeletedOrder_isRejectedWithoutSending() {
        given(order.isDeleted()).willReturn(true);

        assertThatThrownBy(() -> smsService.resend(1L))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        verify(smsClientFactory, never()).getByProvider(any());
        verify(smsMessageRepository, never()).save(any());
    }

    @Test
    void resend_onNormalOrder_resendsFailedMessages() {
        given(order.isDeleted()).willReturn(false);
        given(smsSettingsRepository.get()).willReturn(settings);
        given(settings.resolveTemplate(any())).willReturn("template");
        given(settings.getProvider()).willReturn("ALIGO");
        given(smsClientFactory.getByProvider("ALIGO")).willReturn(smsClient);
        given(failedMessage.getReceiver()).willReturn("01012345678");
        given(smsClient.send(any(), any(), any(), any(), any(), any(), any())).willReturn(new SmsResult(true, "msg-1", null));

        smsService.resend(1L);

        verify(smsClient).send(any(), any(), any(), any(), any(), any(), any());
        verify(smsMessageRepository).save(any(SmsMessage.class));
    }
}
