package com.greenlink.core.application.sms.service;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.AlimtalkRequest;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.AlimtalkClient;
import com.greenlink.core.domain.sms.port.AlimtalkClient.AlimtalkSendResult;
import com.greenlink.core.domain.sms.port.AlimtalkClientFactory;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AlimtalkServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CompanyIntegrationRepository companyIntegrationRepository;
    @Mock CompanyRepository companyRepository;
    @Mock AlimtalkClientFactory alimtalkClientFactory;
    @Mock AlimtalkClient alimtalkClient;
    @Mock SmsMessageRepository smsMessageRepository;
    @Mock Order order;
    @Mock Company company;

    AlimtalkService service;

    @BeforeEach
    void setUp() {
        service = new AlimtalkService(orderRepository, companyIntegrationRepository, companyRepository,
                alimtalkClientFactory, smsMessageRepository);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(order.getOrderingCompanyId()).willReturn(10L);
    }

    private CompanyIntegration integration(String provider, boolean enabled, String senderKey) {
        return CompanyIntegration.of(1L, 10L, provider, "KEY", "02-1234-5678", "baro_id",
                enabled, "T_FUNERAL", "T_WEDDING", "T_ETC", senderKey);
    }

    private void stubOrder() {
        given(order.getEventType()).willReturn("기타");
        given(order.isSendPhotoToOrderer()).willReturn(true);
        given(order.getOrdererPhone()).willReturn("01012345678");
        given(order.getRecipientName()).willReturn("홍길동");
        given(order.getOrderNumber()).willReturn("ORD-1");
        given(order.getPlaceName()).willReturn("서울병원");
        given(order.getPhotoToken()).willReturn("tok");
        given(companyRepository.findById(10L)).willReturn(Optional.of(company));
        given(company.getCompanyName()).willReturn("온우림");
    }

    @Test
    void 바로빌_연동이면_회사_사업자번호로_바로빌_클라이언트에_위임한다() {
        stubOrder();
        given(company.getBusinessNo()).willReturn("222-22-22222");
        given(companyIntegrationRepository.findByCompanyId(10L))
                .willReturn(Optional.of(integration("BAROBILL", true, "@channel")));
        given(alimtalkClientFactory.getByProvider("BAROBILL")).willReturn(alimtalkClient);
        given(alimtalkClient.send(any())).willReturn(new AlimtalkSendResult(true, false, "SK-1"));

        service.send(1L);

        ArgumentCaptor<AlimtalkRequest> req = ArgumentCaptor.forClass(AlimtalkRequest.class);
        verify(alimtalkClient).send(req.capture());
        assertThat(req.getValue().apiKey()).isEqualTo("KEY");
        assertThat(req.getValue().userId()).isEqualTo("baro_id");
        assertThat(req.getValue().corpNum()).isEqualTo("2222222222");
        assertThat(req.getValue().senderKey()).isEqualTo("@channel");
        assertThat(req.getValue().templateCode()).isEqualTo("T_ETC");
        assertThat(req.getValue().receiver()).isEqualTo("01012345678");
        assertThat(req.getValue().variables().companyName()).isEqualTo("온우림");
        ArgumentCaptor<SmsMessage> saved = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(SmsStatus.SUCCESS);
    }

    @Test
    void 바로빌_연동인데_사업자번호가_없으면_실패_이력만_남긴다() {
        stubOrder();
        given(company.getBusinessNo()).willReturn(null);
        given(companyIntegrationRepository.findByCompanyId(10L))
                .willReturn(Optional.of(integration("BAROBILL", true, "@channel")));
        given(alimtalkClientFactory.getByProvider("BAROBILL")).willReturn(alimtalkClient);

        service.send(1L);

        verify(alimtalkClient, never()).send(any());
        ArgumentCaptor<SmsMessage> saved = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(SmsStatus.FAIL);
        assertThat(saved.getValue().getFailReason()).contains("사업자번호");
    }

    @Test
    void 그_외_제공사는_사업자번호_없이_기존_클라이언트로_발송한다() {
        stubOrder();
        given(companyIntegrationRepository.findByCompanyId(10L))
                .willReturn(Optional.of(integration("MESSAGEME", true, "senderkey")));
        given(alimtalkClientFactory.getByProvider("MESSAGEME")).willReturn(alimtalkClient);
        given(alimtalkClient.send(any())).willReturn(new AlimtalkSendResult(false, false, "999"));

        service.send(1L);

        ArgumentCaptor<AlimtalkRequest> req = ArgumentCaptor.forClass(AlimtalkRequest.class);
        verify(alimtalkClient).send(req.capture());
        assertThat(req.getValue().corpNum()).isNull();
        ArgumentCaptor<SmsMessage> saved = ArgumentCaptor.forClass(SmsMessage.class);
        verify(smsMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(SmsStatus.FAIL);
    }

    @Test
    void 알림톡이_비활성이거나_채널키가_없으면_발송하지_않는다() {
        given(companyIntegrationRepository.findByCompanyId(10L))
                .willReturn(Optional.of(integration("BAROBILL", false, "@channel")))
                .willReturn(Optional.of(integration("BAROBILL", true, " ")));

        service.send(1L);
        service.send(1L);

        verifyNoInteractions(alimtalkClientFactory);
        verify(smsMessageRepository, times(0)).save(any());
    }
}
