package com.greenlink.admin.adapter.out.sms;

import com.greenlink.admin.adapter.out.fax.BarobillFtpUploader;
import com.greenlink.core.domain.fax.port.FaxGateway.FaxAccount;
import com.greenlink.core.domain.fax.port.FaxGateway.FaxSendResult;
import com.greenlink.core.domain.fax.port.FaxGateway.FaxState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BarobillFaxClientTest {

    static final String SMS_URL = "https://testws.baroservice.com/SMS.asmx";
    static final String FAX_URL = "https://testws.baroservice.com/FAX.asmx";

    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    BarobillFtpUploader uploader = mock(BarobillFtpUploader.class);
    BarobillFaxClient client;

    @BeforeEach
    void setUp() {
        client = new BarobillFaxClient(new BarobillProperties(SMS_URL), restTemplate, uploader);
    }

    private static FaxAccount account(String serviceUrl) {
        return new FaxAccount("CERT-KEY", "123-45-67890", "baro_id", "02-123-4567", serviceUrl,
                "ftp.example.com", 21, "ftpuser", "ftppass", "/fax");
    }

    private static String response(String method, String result) {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                + "<" + method + "Response xmlns=\"http://ws.baroservice.com/\"><" + method + "Result>" + result
                + "</" + method + "Result></" + method + "Response></soap:Body></soap:Envelope>";
    }

    @Test
    void FTP로_올린_뒤_SOAP으로_발송하고_SendKey를_반환한다() throws Exception {
        server.expect(requestTo(FAX_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/SendFaxFromFTPEx\""))
                .andExpect(content().string(allOf(
                        containsString("<CERTKEY>CERT-KEY</CERTKEY>"),
                        containsString("<CorpNum>1234567890</CorpNum>"),
                        containsString("<FileNames><string>order-1.pdf</string></FileNames>"),
                        containsString("<FromNumber>021234567</FromNumber>"),
                        containsString("<ToNumber>021112222</ToNumber>"),
                        containsString("<RefKey>fax-1-1</RefKey>"))))
                .andRespond(withSuccess(response("SendFaxFromFTPEx", "SK-1001"), MediaType.TEXT_XML));

        FaxSendResult result = client.send(account(null), "order-1.pdf", new byte[]{1}, "02-111-2222", "수주사", "fax-1-1");

        assertThat(result.success()).isTrue();
        assertThat(result.sendKey()).isEqualTo("SK-1001");
        verify(uploader).upload(eq("ftp.example.com"), eq(21), eq("ftpuser"), eq("ftppass"), eq("/fax"), eq("order-1.pdf"), any());
        server.verify();
    }

    @Test
    void 설정에_저장한_URL을_우선_사용한다() {
        server.expect(requestTo("https://ws.baroservice.com/FAX.asmx"))
                .andRespond(withSuccess(response("SendFaxFromFTPEx", "SK-1"), MediaType.TEXT_XML));

        FaxSendResult result = client.send(account("https://ws.baroservice.com/FAX.asmx"), "a.pdf", new byte[]{1},
                "021112222", "수주사", "k");

        assertThat(result.success()).isTrue();
        server.verify();
    }

    @Test
    void 음수_응답은_오류로_처리한다() {
        server.expect(requestTo(FAX_URL))
                .andRespond(withSuccess(response("SendFaxFromFTPEx", "-10001"), MediaType.TEXT_XML));

        FaxSendResult result = client.send(account(null), "a.pdf", new byte[]{1}, "021112222", "수주사", "k");

        assertThat(result.success()).isFalse();
        assertThat(result.failReason()).contains("-10001");
    }

    @Test
    void FTP_업로드에_실패하면_SOAP을_호출하지_않는다() throws Exception {
        doThrow(new IOException("FTP 로그인 실패")).when(uploader).upload(any(), any(), any(), any(), any(), any(), any());

        FaxSendResult result = client.send(account(null), "a.pdf", new byte[]{1}, "021112222", "수주사", "k");

        assertThat(result.success()).isFalse();
        assertThat(result.failReason()).contains("업로드 실패");
        server.verify();
    }

    @Test
    void 전송상태_코드를_해석한다() {
        assertThat(BarobillFaxClient.mapState(1)).isEqualTo(FaxState.DELIVERED);
        assertThat(BarobillFaxClient.mapState(-30001)).isEqualTo(FaxState.FAILED);
        assertThat(BarobillFaxClient.mapState(0)).isEqualTo(FaxState.PENDING);
    }

    @Test
    void 전송상태를_조회한다() {
        server.expect(requestTo(FAX_URL))
                .andExpect(content().string(containsString("<SendKey>SK-1</SendKey>")))
                .andRespond(withSuccess(response("GetFaxSendState", "1"), MediaType.TEXT_XML));

        assertThat(client.getState(account(null), "SK-1")).isEqualTo(FaxState.DELIVERED);
    }
}
