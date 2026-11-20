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

    @Test
    void 조립된_XML_본문으로_요청을_만든다() {
        String xml = BarobillSoap.buildEnvelopeXml("SendATKakaotalkEx",
                BarobillSoap.element("CERTKEY", "K") + "<KakaotalkMessage>" + BarobillSoap.element("Message", "a&b")
                        + "</KakaotalkMessage>");

        assertThat(xml).contains("<SendATKakaotalkEx xmlns=\"http://ws.baroservice.com/\">");
        assertThat(xml).contains("<CERTKEY>K</CERTKEY><KakaotalkMessage><Message>a&amp;b</Message></KakaotalkMessage>");
    }

    @Test
    void 템플릿_목록_응답을_파싱한다() {
        String xml = "<?xml version=\"1.0\"?><soap:Envelope xmlns:soap=\"x\"><soap:Body>"
                + "<GetKakaotalkTemplatesResponse xmlns=\"http://ws.baroservice.com/\"><GetKakaotalkTemplatesResult>"
                + "<KakaotalkTemplate><ChannelId>@ch</ChannelId><TemplateName>T1</TemplateName>"
                + "<TemplateContent>안녕 #{받는분}</TemplateContent><TemplateTitle></TemplateTitle>"
                + "<Buttons><KakaotalkButton><Name>사진</Name><ButtonType>WL</ButtonType>"
                + "<Url1>https://x/#{사진링크}</Url1><Url2></Url2></KakaotalkButton></Buttons></KakaotalkTemplate>"
                + "<KakaotalkTemplate><TemplateName>T2</TemplateName><TemplateContent>c2</TemplateContent></KakaotalkTemplate>"
                + "</GetKakaotalkTemplatesResult></GetKakaotalkTemplatesResponse></soap:Body></soap:Envelope>";

        var templates = BarobillSoap.parseTemplates(xml);

        assertThat(templates).hasSize(2);
        assertThat(templates.get(0).templateName()).isEqualTo("T1");
        assertThat(templates.get(0).content()).isEqualTo("안녕 #{받는분}");
        assertThat(templates.get(0).buttons()).hasSize(1);
        assertThat(templates.get(0).buttons().get(0).url1()).isEqualTo("https://x/#{사진링크}");
        assertThat(templates.get(1).buttons()).isEmpty();
    }

    @Test
    void 파싱할_수_없는_템플릿_응답은_빈_목록이다() {
        assertThat(BarobillSoap.parseTemplates("not xml")).isEmpty();
        assertThat(BarobillSoap.parseTemplates(null)).isEmpty();
        assertThat(BarobillSoap.parseTemplates("<!DOCTYPE a [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><a>&x;</a>")).isEmpty();
    }
}
