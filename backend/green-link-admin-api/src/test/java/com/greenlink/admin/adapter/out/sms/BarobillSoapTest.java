package com.greenlink.admin.adapter.out.sms;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BarobillSoapTest {

    @Test
    void 요청_XML은_파라미터_순서를_유지하고_특수문자를_이스케이프한다() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("CERTKEY", "KEY");
        params.put("Contents", "A & B <tag> \"q\" 'a'");

        String xml = BarobillSoap.buildEnvelope("SendSMSMessage", params);

        assertThat(xml).contains("<SendSMSMessage xmlns=\"http://ws.baroservice.com/\">");
        assertThat(xml).contains("<CERTKEY>KEY</CERTKEY><Contents>A &amp; B &lt;tag&gt; &quot;q&quot; &apos;a&apos;</Contents>");
    }

    @Test
    void XML에서_허용되지_않는_제어문자는_제거한다() {
        assertThat(BarobillSoap.escape("a\u0001b\u000Bc\nd")).isEqualTo("abc\nd");
    }

    @Test
    void 응답에서_Result_값을_추출한다() {
        String xml = "<?xml version=\"1.0\"?><soap:Envelope xmlns:soap=\"x\"><soap:Body>"
                + "<SendSMSMessageResponse xmlns=\"http://ws.baroservice.com/\">"
                + "<SendSMSMessageResult>20260101-123</SendSMSMessageResult>"
                + "</SendSMSMessageResponse></soap:Body></soap:Envelope>";

        assertThat(BarobillSoap.parseResult(xml, "SendSMSMessage")).contains("20260101-123");
    }

    @Test
    void 빈_Result와_없는_Result를_구분한다() {
        String empty = "<R><SendSMSMessageResult /></R>";
        String missing = "<R><Other>1</Other></R>";

        assertThat(BarobillSoap.parseResult(empty, "SendSMSMessage")).contains("");
        assertThat(BarobillSoap.parseResult(missing, "SendSMSMessage")).isEmpty();
        assertThat(BarobillSoap.parseResult(null, "SendSMSMessage")).isEmpty();
    }

    @Test
    void 음수_숫자는_오류코드로_판정한다() {
        assertThat(BarobillSoap.isErrorCode("-10001")).isTrue();
        assertThat(BarobillSoap.isErrorCode("20260101-123")).isFalse();
        assertThat(BarobillSoap.isErrorCode("12345")).isFalse();
    }

    @Test
    void 한글은_2byte_영문은_1byte로_계산한다() {
        assertThat(BarobillSoap.byteLength("가나다abc")).isEqualTo(9);
        assertThat(BarobillSoap.truncateBytes("가나다라", 5)).isEqualTo("가나");
    }
}
