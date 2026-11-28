package com.greenlink.core.application.auth.service;

import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.phoneverification.port.PhoneVerificationRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsMessageType;
import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsClientFactory;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationServiceTest {

    @Mock PhoneVerificationRepository verificationRepository;
    @Mock AdminRepository adminRepository;
    @Mock SmsClientFactory smsClientFactory;
    @Mock SmsSettingsRepository smsSettingsRepository;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock SmsClient smsClient;
    @Mock SmsSettings settings;

    PhoneVerificationService service;

    @BeforeEach
    void setUp() {
        service = new PhoneVerificationService(verificationRepository, adminRepository, smsClientFactory,
                smsSettingsRepository, smsMessageRepository, passwordEncoder);
        given(smsSettingsRepository.get()).willReturn(settings);
        given(settings.getProvider()).willReturn("ALIGO");
        given(smsClientFactory.getByProvider("ALIGO")).willReturn(smsClient);
    }

    private SmsMessage savedHistory() {
        ArgumentCaptor<SmsMessage> captor = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void 발송에_성공하면_본인인증_이력이_SUCCESS로_저장되고_인증번호는_저장되지_않는다() {
        given(smsClient.send(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new SmsResult(true, "msg-1", null));

        service.send("01012345678");

        SmsMessage history = savedHistory();
        assertThat(history.getType()).isEqualTo(SmsMessageType.PHONE_VERIFICATION);
        assertThat(history.getStatus()).isEqualTo(SmsStatus.SUCCESS);
        assertThat(history.getOrderId()).isNull();
        assertThat(history.getReceiver()).isEqualTo("01012345678");
        assertThat(history.getMessage()).isEqualTo("[본인인증] 인증번호 발송");
    }

    @Test
    void 연동사가_거절하면_FAIL_이력과_사유가_저장되고_SMS_SEND_FAILED가_발생한다() {
        given(smsClient.send(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(SmsResult.rejected("155: 발신번호 미등록"));

        assertThatThrownBy(() -> service.send("01012345678"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(AuthErrorCode.SMS_SEND_FAILED));

        SmsMessage history = savedHistory();
        assertThat(history.getStatus()).isEqualTo(SmsStatus.FAIL);
        assertThat(history.getFailReason()).isEqualTo("155: 발신번호 미등록");
    }

    @Test
    void 발송_클라이언트가_예외를_던져도_FAIL_이력이_저장된다() {
        given(smsClient.send(any(), any(), any(), any(), any(), any(), any()))
                .willThrow(new IllegalStateException("연결 거부"));

        assertThatThrownBy(() -> service.send("01012345678"))
                .isInstanceOf(BusinessException.class);

        SmsMessage history = savedHistory();
        assertThat(history.getStatus()).isEqualTo(SmsStatus.FAIL);
        assertThat(history.getFailReason()).isEqualTo("연결 거부");
    }

    @Test
    void 이력_저장이_실패해도_발송이_성공했으면_예외없이_진행한다() {
        given(smsClient.send(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new SmsResult(true, "msg-1", null));
        doThrow(new IllegalStateException("db down")).when(smsMessageRepository).save(any());

        service.send("01012345678");

        verify(smsMessageRepository).save(any());
    }
}
