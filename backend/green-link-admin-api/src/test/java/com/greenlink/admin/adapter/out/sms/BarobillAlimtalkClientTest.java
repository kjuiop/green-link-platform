package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.model.AlimtalkRequest;
import com.greenlink.core.domain.sms.model.AlimtalkRequest.AlimtalkVariables;
import com.greenlink.core.domain.sms.port.AlimtalkClient.AlimtalkSendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BarobillAlimtalkClientTest {

    static final String SMS_URL = "https://testws.baroservice.com/SMS.asmx";
    static final String URL = "https://testws.baroservice.com/KAKAOTALK.asmx";

    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    BarobillAlimtalkClient client;

    @BeforeEach
    void setUp() {
        client = new BarobillAlimtalkClient(new BarobillProperties(SMS_URL), restTemplate);
    }

    private static AlimtalkRequest request() {
        return new AlimtalkRequest("CERT-KEY", "baro_id", "123-45-67890", "@channel", "TPL_ETC",
                "02-1234-5678", "010-1234-5678",
                new AlimtalkVariables("온우림", "홍길동", "ORD-1", "서울병원", "tok-123"));
    }

    private static String templates(String content, String buttonUrl) {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                + "<GetKakaotalkTemplatesResponse xmlns=\"http://ws.baroservice.com/\"><GetKakaotalkTemplatesResult>"
                + "<KakaotalkTemplate><TemplateName>OTHER</TemplateName><TemplateContent>x</TemplateContent></KakaotalkTemplate>"
                + "<KakaotalkTemplate><TemplateName>TPL_ETC</TemplateName><TemplateTitle></TemplateTitle>"
                + "<TemplateContent>" + content + "</TemplateContent>"
                + "<Buttons><KakaotalkButton><Name>사진 보기</Name><ButtonType>WL</ButtonType>"
                + "<Url1>" + buttonUrl + "</Url1><Url2></Url2></KakaotalkButton></Buttons></KakaotalkTemplate>"
                + "</GetKakaotalkTemplatesResult></GetKakaotalkTemplatesResponse></soap:Body></soap:Envelope>";
    }

    private static String sendResponse(String result) {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                + "<SendATKakaotalkExResponse xmlns=\"http://ws.baroservice.com/\"><SendATKakaotalkExResult>"
                + result + "</SendATKakaotalkExResult></SendATKakaotalkExResponse></soap:Body></soap:Envelope>";
    }

    private void expectTemplateLookup(String content, String buttonUrl) {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/GetKakaotalkTemplates\""))
                .andExpect(content().string(allOf(
                        containsString("<CERTKEY>CERT-KEY</CERTKEY>"),
                        containsString("<CorpNum>1234567890</CorpNum>"),
                        containsString("<ChannelId>@channel</ChannelId>"))))
                .andRespond(withSuccess(templates(content, buttonUrl), MediaType.TEXT_XML));
    }

    @Test
    void 템플릿을_조회해_변수를_치환한_뒤_발송한다() {
        expectTemplateLookup("#{업체명}입니다. #{받는분}님 #{주문번호} #{장소} 배송완료", "https://x.com/delivery/#{사진링크}");
        server.expect(requestTo(URL))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/SendATKakaotalkEx\""))
                .andExpect(content().string(allOf(
                        containsString("<SenderID>baro_id</SenderID>"),
                        containsString("<YellowId>@channel</YellowId>"),
                        containsString("<TemplateName>TPL_ETC</TemplateName>"),
                        containsString("<SmsReply></SmsReply>"),
                        containsString("<SmsSenderNum></SmsSenderNum>"),
                        containsString("<ReceiverName>홍길동</ReceiverName>"),
                        containsString("<ReceiverNum>01012345678</ReceiverNum>"),
                        containsString("<Message>온우림입니다. 홍길동님 ORD-1 서울병원 배송완료</Message>"),
                        containsString("<SmsMessage></SmsMessage>"),
                        containsString("<Url1>https://x.com/delivery/tok-123</Url1>"))))
                .andRespond(withSuccess(sendResponse("20261120-001"), MediaType.TEXT_XML));

        AlimtalkSendResult result = client.send(request());

        assertThat(result.success()).isTrue();
        assertThat(result.resultCode()).isEqualTo("20261120-001");
        server.verify();
    }

    @Test
    void 템플릿이_없으면_발송하지_않고_실패한다() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(templates("c", "u").replace("TPL_ETC", "ANOTHER"), MediaType.TEXT_XML));

        AlimtalkSendResult result = client.send(request());

        assertThat(result.success()).isFalse();
        assertThat(result.resultCode()).contains("템플릿을 찾을 수 없습니다");
        server.verify();
    }

    @Test
    void 치환되지_않은_변수가_남으면_발송하지_않는다() {
        expectTemplateLookup("#{알수없는변수} 안내", "https://x.com/#{사진링크}");

        AlimtalkSendResult result = client.send(request());

        assertThat(result.success()).isFalse();
        assertThat(result.resultCode()).contains("치환되지 않은 변수");
        server.verify();
    }

    @Test
    void 바로빌_오류코드는_실패로_처리한다() {
        expectTemplateLookup("#{받는분}님", "https://x.com/#{사진링크}");
        server.expect(requestTo(URL))
                .andRespond(withSuccess(sendResponse("-60001"), MediaType.TEXT_XML));

        AlimtalkSendResult result = client.send(request());

        assertThat(result.success()).isFalse();
        assertThat(result.resultCode()).isEqualTo("바로빌 오류 (-60001)");
    }

    @Test
    void 필수_계정정보가_없으면_호출하지_않고_실패한다() {
        AlimtalkRequest noKey = new AlimtalkRequest(null, "baro_id", "1234567890", "@channel", "TPL_ETC",
                "02", "010", new AlimtalkVariables("a", "b", "c", "d", "e"));

        AlimtalkSendResult result = client.send(noKey);

        assertThat(result.success()).isFalse();
        server.verify();
    }

    @Test
    void HTTP_오류는_실패로_처리한다() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        AlimtalkSendResult result = client.send(request());

        assertThat(result.success()).isFalse();
        assertThat(result.resultCode()).isEqualTo("바로빌 응답 오류 (HTTP 500)");
    }

    @Test
    void 알림톡_주소는_SMS_주소에서_파생한다() {
        assertThat(new BarobillProperties("https://ws.baroservice.com/SMS.asmx").kakaotalkUrl())
                .isEqualTo("https://ws.baroservice.com/KAKAOTALK.asmx");
    }
}
