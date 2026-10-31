package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.sms.dto.SmsResult;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClient;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock SmsClient smsClient;
    @Mock SmsSettingsRepository smsSettingsRepository;
    @Mock Order order;
    @Mock SmsSettings settings;

    SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(orderRepository, smsMessageRepository, smsClient, smsSettingsRepository);
        given(smsMessageRepository.findAllByOrderId(1L)).willReturn(List.of());
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(smsSettingsRepository.get()).willReturn(settings);
        given(settings.resolveTemplate(any())).willReturn("template");
        given(order.isSendPhotoToOrderer()).willReturn(true);
        given(order.getOrdererPhone()).willReturn("01012345678");
        given(order.getMessageSchedule()).willReturn("SCHEDULED");
        given(order.getDeliveryDate()).willReturn("2099-12-31");
        given(order.getScheduledMessageHour()).willReturn("10");
        given(order.getScheduledMessageMinute()).willReturn("00");
    }

    @Test
    void 예약발송이_알리고에_거절되면_즉시발송으로_폴백하고_성공처리한다() {
        given(smsClient.send(any(), any(), any(), any(), eq("20991231"), eq("1000"), any()))
                .willReturn(SmsResult.rejected("예약 시간 오류"));
        given(smsClient.send(any(), any(), any(), any(), isNull(), isNull(), any()))
                .willReturn(new SmsResult(true, "msg-1", null));

        smsService.send(1L, null);

        ArgumentCaptor<SmsMessage> captor = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SmsStatus.SUCCESS);
        verify(smsClient, times(2)).send(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void 예외로_실패한_예약발송은_중복발송_위험이_있어_폴백하지_않는다() {
        given(smsClient.send(any(), any(), any(), any(), eq("20991231"), eq("1000"), any()))
                .willReturn(new SmsResult(false, null, "timeout"));

        smsService.send(1L, null);

        ArgumentCaptor<SmsMessage> captor = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SmsStatus.FAIL);
        verify(smsClient, times(1)).send(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void 폴백_즉시발송도_실패하면_FAIL로_기록한다() {
        given(smsClient.send(any(), any(), any(), any(), anyString(), anyString(), any()))
                .willReturn(SmsResult.rejected("예약 시간 오류"));
        given(smsClient.send(any(), any(), any(), any(), isNull(), isNull(), any()))
                .willReturn(SmsResult.rejected("인증 오류"));

        smsService.send(1L, null);

        ArgumentCaptor<SmsMessage> captor = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SmsStatus.FAIL);
        assertThat(captor.getValue().getFailReason()).isEqualTo("인증 오류");
    }
}
