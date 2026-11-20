package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Account;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.IssueRequest;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.IssueResult;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Party;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BarobillTaxInvoiceClientTest {

    static final String TI_URL = "https://testws.baroservice.com/TI.asmx";

    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    BarobillTaxInvoiceClient client;

    static final Account ACCOUNT = new Account("CERT-KEY", "123-45-67890", "baro_id");

    @BeforeEach
    void setUp() {
        client = new BarobillTaxInvoiceClient(new BarobillProperties("https://testws.baroservice.com/SMS.asmx"), restTemplate);
    }

    private static IssueRequest request() {
        return new IssueRequest(
                new Party("1234567890", "온우림", "대표", "서울", "sup@test.com"),
                new Party("987-65-43210", "발주사 & co", "홍길동", "부산 3층", "billing@test.com"),
                "GL-5-1", LocalDate.of(2026, 11, 21), "포인트 충전", 100000L);
    }

    private static String response(String method, String result) {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                + "<" + method + "Response xmlns=\"http://ws.baroservice.com/\"><" + method + "Result>" + result
                + "</" + method + "Result></" + method + "Response></soap:Body></soap:Envelope>";
    }

    @Test
    void 면세_계산서_요청을_보내고_성공하면_관리번호를_문서키로_반환한다() {
        server.expect(requestTo(TI_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/RegistAndIssueTaxInvoice\""))
                .andExpect(content().string(allOf(
                        containsString("<CERTKEY>CERT-KEY</CERTKEY>"),
                        containsString("<CorpNum>1234567890</CorpNum>"),
                        containsString("<TaxType>3</TaxType>"),
                        containsString("<WriteDate>20261121</WriteDate>"),
                        containsString("<AmountTotal>100000</AmountTotal>"),
                        containsString("<TaxTotal>0</TaxTotal>"),
                        containsString("<TotalAmount>100000</TotalAmount>"),
                        containsString("<MgtNum>GL-5-1</MgtNum>"),
                        containsString("<CorpNum>9876543210</CorpNum>"),
                        containsString("<CorpName>발주사 &amp; co</CorpName>"),
                        containsString("<Email>billing@test.com</Email>"))))
                .andRespond(withSuccess(response("RegistAndIssueTaxInvoice", "1"), MediaType.TEXT_XML));

        IssueResult result = client.issue(ACCOUNT, request());

        assertThat(result.success()).isTrue();
        assertThat(result.docKey()).isEqualTo("GL-5-1");
        server.verify();
    }

    @Test
    void 음수_응답은_실패로_처리한다() {
        server.expect(requestTo(TI_URL))
                .andRespond(withSuccess(response("RegistAndIssueTaxInvoice", "-11010"), MediaType.TEXT_XML));

        IssueResult result = client.issue(ACCOUNT, request());

        assertThat(result.success()).isFalse();
        assertThat(result.failReason()).contains("-11010");
    }

    @Test
    void 발행_상태_코드가_기준_이상이면_발행된_것으로_본다() {
        server.expect(requestTo(TI_URL))
                .andExpect(header("SOAPAction", "\"http://ws.baroservice.com/GetTaxInvoiceStateEX\""))
                .andRespond(withSuccess("<r><BarobillState>3011</BarobillState></r>", MediaType.TEXT_XML));

        Optional<String> key = client.findIssuedDocKey(ACCOUNT, "GL-5-1");

        assertThat(key).contains("GL-5-1");
    }

    @Test
    void 상태를_찾을_수_없으면_빈_값이다() {
        assertThat(BarobillTaxInvoiceClient.parseIssuedState("<r><BarobillState>-11010</BarobillState></r>")).isFalse();
        assertThat(BarobillTaxInvoiceClient.parseIssuedState("<r/>")).isFalse();
    }
}
