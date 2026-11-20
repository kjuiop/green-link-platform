package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.model.SmsAccount;
import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BarobillSmsClientTest {

    static final String URL = "https://testws.baroservice.com/SMS.asmx";
    static final SmsAccount ACCOUNT = new SmsAccount("BAROBILL", "CERT-KEY", "02-1234-5678", "baro_id", "123-45-67890");

    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    BarobillSmsClient client;

    @BeforeEach
    void setUp() {
        client = new BarobillSmsClient(new BarobillProperties(URL), restTemplate);
    }

    private static String response(String method, String result) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body><" + method + "Response xmlns=\"http://ws.baroservice.com/\"><" + method + "Result>"
                + result + "</" + method + "Result></" + method + "Response></soap:Body></soap:Envelope>";
    }

    @Test
    void 짧은_메시지는_SMS로_발송한다() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/SendSMSMessage\""))
                .andExpect(content().string(allOf(
                        containsString("<CERTKEY>CERT-KEY</CERTKEY>"),
                        containsString("<CorpNum>1234567890</CorpNum>"),
                        containsString("<SenderID>baro_id</SenderID>"),
                        containsString("<FromNumber>0212345678</FromNumber>"),
                        containsString("<ToNumber>01012345678</ToNumber>"),
                        containsString("<Contents>안녕하세요</Contents>"),
                        containsString("<SendDT></SendDT>"))))
                .andRespond(withSuccess(response("SendSMSMessage", "SK-1"), MediaType.TEXT_XML));

        SmsResult result = client.sendWithAccount(ACCOUNT, "010-1234-5678", "안녕하세요", null, null, null, null);

        assertThat(result.success()).isTrue();
        assertThat(result.msgId()).isEqualTo("SK-1");
        server.verify();
    }

    @Test
    void 비ASCII_90byte를_넘으면_LMS로_발송한다() {
        server.expect(requestTo(URL))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/SendLMSMessage\""))
                .andExpect(content().string(containsString("<Subject>제목</Subject>")))
                .andRespond(withSuccess(response("SendLMSMessage", "SK-2"), MediaType.TEXT_XML));

        SmsResult result = client.sendWithAccount(ACCOUNT, "01012345678", "가".repeat(46), null, null, null, "제목");

        assertThat(result.success()).isTrue();
        server.verify();
    }

    @Test
    void 예약_시각은_SendDT로_변환한다() {
        server.expect(requestTo(URL))
                .andExpect(content().string(containsString("<SendDT>20991231100000</SendDT>")))
                .andRespond(withSuccess(response("SendSMSMessage", "SK-3"), MediaType.TEXT_XML));

        SmsResult result = client.sendWithAccount(ACCOUNT, "01012345678", "hi", null, "20991231", "1000", null);

        assertThat(result.success()).isTrue();
        server.verify();
    }

    @Test
    void 이미지가_있으면_MMS로_JPG_변환_후_발송한다() throws Exception {
        BufferedImage img = new BufferedImage(1600, 1200, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(img, "png", png);
        server.expect(requestTo("https://img.example.com/a.png"))
                .andRespond(withSuccess(png.toByteArray(), MediaType.IMAGE_PNG));
        server.expect(requestTo(URL))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/SendMMSMessage\""))
                .andExpect(content().string(allOf(
                        containsString("<TXTSubject>배송완료</TXTSubject>"),
                        containsString("<TXTMESSAGE>사진</TXTMESSAGE>"),
                        containsString("<ImageFile>"))))
                .andRespond(withSuccess(response("SendMMSMessage", "SK-4"), MediaType.TEXT_XML));

        SmsResult result = client.sendWithAccount(ACCOUNT, "01012345678", "사진", "https://img.example.com/a.png", null, null, "배송완료");

        assertThat(result.success()).isTrue();
        server.verify();
    }

    @Test
    void 이미지_변환에_실패하면_텍스트만_보내지_않고_실패로_반환한다() {
        server.expect(requestTo("https://img.example.com/bad.png"))
                .andRespond(withSuccess("not-an-image".getBytes(), MediaType.IMAGE_PNG));

        SmsResult result = client.sendWithAccount(ACCOUNT, "01012345678", "사진", "https://img.example.com/bad.png", null, null, null);

        assertThat(result.success()).isFalse();
        assertThat(result.failReason()).contains("이미지 변환 실패");
        server.verify();
    }

    @Test
    void 음수_오류코드는_거절로_반환한다() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(response("SendSMSMessage", "-10001"), MediaType.TEXT_XML));

        SmsResult result = client.sendWithAccount(ACCOUNT, "01012345678", "hi", null, null, null, null);

        assertThat(result.success()).isFalse();
        assertThat(result.rejectedByProvider()).isTrue();
        assertThat(result.failReason()).contains("-10001");
    }

    @Test
    void 계정_정보가_없으면_호출하지_않고_실패한다() {
        SmsAccount incomplete = new SmsAccount("BAROBILL", "", "0212345678", "baro_id", "1234567890");

        SmsResult result = client.sendWithAccount(incomplete, "01012345678", "hi", null, null, null, null);

        assertThat(result.success()).isFalse();
        assertThat(result.failReason()).contains("설정되지 않았습니다");
        server.verify();
    }

    @Test
    void 서버_오류는_예외_없이_실패로_반환하고_인증키를_노출하지_않는다() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        SmsResult result = client.sendWithAccount(ACCOUNT, "01012345678", "hi", null, null, null, null);

        assertThat(result.success()).isFalse();
        assertThat(result.failReason()).contains("HTTP 500").doesNotContain("CERT-KEY");
    }

    @Test
    void 발신번호_등록여부와_전송상태를_조회한다() {
        server.expect(requestTo(URL))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/CheckSMSFromNumber\""))
                .andRespond(withSuccess(response("CheckSMSFromNumber", "1"), MediaType.TEXT_XML));
        server.expect(requestTo(URL))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/GetSMSSendState\""))
                .andRespond(withSuccess(response("GetSMSSendState", "-20001"), MediaType.TEXT_XML));

        assertThat(client.checkSenderNumber(ACCOUNT)).isEqualTo(1);
        assertThat(client.getSendState(ACCOUNT, "SK-1")).isEqualTo(-20001);
    }

    @Test
    void 조회_통신_실패는_BusinessException으로_변환한다() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.getSendState(ACCOUNT, "SK-1")).isInstanceOf(BusinessException.class);
    }
}
