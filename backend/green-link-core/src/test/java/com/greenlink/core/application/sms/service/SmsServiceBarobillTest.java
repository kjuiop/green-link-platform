package com.greenlink.core.application.sms.service;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.SmsAccount;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClient;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsServiceBarobillTest {

    @Mock OrderRepository orderRepository;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock SmsClient smsClient;
    @Mock SmsClientFactory smsClientFactory;
    @Mock CompanyIntegrationRepository companyIntegrationRepository;
    @Mock SmsSettingsRepository smsSettingsRepository;
    @Mock CompanyRepository companyRepository;
    @Mock Order order;
    @Mock Company company;

    SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(orderRepository, smsMessageRepository, smsClientFactory,
                smsSettingsRepository, companyIntegrationRepository, companyRepository);
        SmsSettings global = SmsSettings.of(1L, "02-000-0000", "온우림", "f", "w", "o",
                "BAROBILL", "GLOBAL-KEY", "global_id", "1111111111");
        given(smsMessageRepository.findAllByOrderId(1L)).willReturn(List.of());
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(smsSettingsRepository.get()).willReturn(global);
        given(smsClientFactory.getByProvider("BAROBILL")).willReturn(smsClient);
        given(order.getOrderingCompanyId()).willReturn(10L);
        given(order.isSendPhotoToOrderer()).willReturn(true);
        given(order.getOrdererPhone()).willReturn("01012345678");
    }

    @Test
    void 업체별_바로빌_계정이_있으면_그_계정으로_발송한다() {
        given(companyIntegrationRepository.findByCompanyId(10L)).willReturn(Optional.of(
                CompanyIntegration.create(10L, "BAROBILL", "COMPANY-KEY", "031-111-1111", "company_id")));
        given(companyRepository.findById(10L)).willReturn(Optional.of(company));
        given(company.getBusinessNo()).willReturn("222-22-22222");
        given(smsClient.sendWithAccount(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new SmsResult(true, "SK-1", null));

        smsService.send(1L, null);

        ArgumentCaptor<SmsAccount> account = ArgumentCaptor.forClass(SmsAccount.class);
        verify(smsClient).sendWithAccount(account.capture(), eq("01012345678"), any(), isNull(), isNull(), isNull(), isNull());
        assertThat(account.getValue()).isEqualTo(
                new SmsAccount("BAROBILL", "COMPANY-KEY", "031-111-1111", "company_id", "2222222222"));
        ArgumentCaptor<SmsMessage> saved = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(SmsStatus.SUCCESS);
    }

    @Test
    void 업체별_계정이_없으면_운영자_기본_계정으로_발송한다() {
        given(companyIntegrationRepository.findByCompanyId(10L)).willReturn(Optional.empty());
        given(smsClient.sendWithAccount(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new SmsResult(true, "SK-2", null));

        smsService.send(1L, null);

        ArgumentCaptor<SmsAccount> account = ArgumentCaptor.forClass(SmsAccount.class);
        verify(smsClient).sendWithAccount(account.capture(), any(), any(), any(), any(), any(), any());
        assertThat(account.getValue()).isEqualTo(
                new SmsAccount("BAROBILL", "GLOBAL-KEY", "02-000-0000", "global_id", "1111111111"));
    }

    @Test
    void 바로빌_예약발송_거절은_알리고처럼_즉시발송으로_폴백하지_않는다() {
        given(companyIntegrationRepository.findByCompanyId(10L)).willReturn(Optional.empty());
        given(order.getMessageSchedule()).willReturn("SCHEDULED");
        given(order.getDeliveryDate()).willReturn("2099-12-31");
        given(order.getScheduledMessageHour()).willReturn("10");
        given(order.getScheduledMessageMinute()).willReturn("00");
        given(smsClient.sendWithAccount(any(), any(), any(), any(), eq("20991231"), eq("1000"), any()))
                .willReturn(SmsResult.rejected("바로빌 오류 (-10001)"));

        smsService.send(1L, null);

        verify(smsClient, never()).send(any(String.class), any(), any(), any(), any(), any(), any());
        ArgumentCaptor<SmsMessage> saved = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(SmsStatus.FAIL);
    }
}
