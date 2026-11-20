package com.greenlink.admin.adapter.out.sms;

import com.greenlink.admin.adapter.out.sms.BarobillSoap.KakaotalkButton;
import com.greenlink.admin.adapter.out.sms.BarobillSoap.KakaotalkTemplate;
import com.greenlink.core.domain.sms.model.AlimtalkRequest;
import com.greenlink.core.domain.sms.model.AlimtalkRequest.AlimtalkVariables;
import com.greenlink.core.domain.sms.port.AlimtalkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class BarobillAlimtalkClient implements AlimtalkClient {

    private static final Logger log = LoggerFactory.getLogger(BarobillAlimtalkClient.class);

    private final BarobillProperties props;
    private final RestTemplate restTemplate;

    public BarobillAlimtalkClient(BarobillProperties props, RestTemplate smsRestTemplate) {
        this.props = props;
        this.restTemplate = smsRestTemplate;
    }

    @Override
    public AlimtalkSendResult send(AlimtalkRequest req) {
        if (isBlank(req.apiKey()) || isBlank(req.corpNum()) || isBlank(req.userId())
                || isBlank(req.senderKey()) || isBlank(req.templateCode())) {
            return fail("바로빌 알림톡 계정 정보가 설정되지 않았습니다.");
        }
        try {
            KakaotalkTemplate template = findTemplate(req);
            if (template == null) {
                return fail("바로빌 알림톡 템플릿을 찾을 수 없습니다: " + req.templateCode());
            }

            Map<String, String> values = substitutions(req.variables());
            String message = apply(template.content(), values);
            String title = apply(template.title(), values);
            StringBuilder buttons = new StringBuilder();
            boolean unresolved = containsPlaceholder(message) || containsPlaceholder(title);
            for (KakaotalkButton b : template.buttons()) {
                String url1 = apply(b.url1(), values);
                String url2 = apply(b.url2(), values);
                unresolved |= containsPlaceholder(url1) || containsPlaceholder(url2);
                buttons.append("<KakaotalkButton>")
                        .append(BarobillSoap.element("Name", b.name()))
                        .append(BarobillSoap.element("ButtonType", b.buttonType()))
                        .append(BarobillSoap.element("Url1", url1))
                        .append(BarobillSoap.element("Url2", url2))
                        .append("</KakaotalkButton>");
            }
            if (unresolved) {
                return fail("바로빌 알림톡 템플릿에 치환되지 않은 변수가 있습니다.");
            }

            String inner = BarobillSoap.element("CERTKEY", req.apiKey())
                    + BarobillSoap.element("CorpNum", digits(req.corpNum()))
                    + BarobillSoap.element("SenderID", req.userId())
                    + BarobillSoap.element("YellowId", req.senderKey())
                    + BarobillSoap.element("TemplateName", req.templateCode())
                    + BarobillSoap.element("SendDT", "")
                    + BarobillSoap.element("SmsReply", "")
                    + BarobillSoap.element("SmsSenderNum", "")
                    + "<KakaotalkMessage>"
                    + BarobillSoap.element("ReceiverName", req.variables().recipientName())
                    + BarobillSoap.element("ReceiverNum", digits(req.receiver()))
                    + BarobillSoap.element("Title", title)
                    + BarobillSoap.element("Message", message)
                    + BarobillSoap.element("SmsMessage", "")
                    + BarobillSoap.element("SmsSubject", "")
                    + "<Buttons>" + buttons + "</Buttons>"
                    + "</KakaotalkMessage>";

            String result = call("SendATKakaotalkEx", inner).flatMap(r -> BarobillSoap.parseResult(r, "SendATKakaotalkEx"))
                    .orElse("");
            if (result.isEmpty()) return fail("바로빌 응답이 올바르지 않습니다.");
            if (BarobillSoap.isErrorCode(result)) return fail("바로빌 오류 (" + result + ")");
            return new AlimtalkSendResult(true, false, result);
        } catch (Exception e) {
            log.warn("[바로빌 알림톡 발송 실패] {}", e.getClass().getSimpleName());
            return fail(describe(e));
        }
    }

    private KakaotalkTemplate findTemplate(AlimtalkRequest req) {
        String inner = BarobillSoap.element("CERTKEY", req.apiKey())
                + BarobillSoap.element("CorpNum", digits(req.corpNum()))
                + BarobillSoap.element("ChannelId", req.senderKey());
        String xml = call("GetKakaotalkTemplates", inner).orElse("");
        return BarobillSoap.parseTemplates(xml).stream()
                .filter(t -> req.templateCode().equals(t.templateName()))
                .findFirst()
                .orElse(null);
    }

    private Optional<String> call(String method, String innerXml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));
        headers.add("SOAPAction", BarobillSoap.soapAction(method));
        byte[] response = restTemplate.exchange(props.kakaotalkUrl(), HttpMethod.POST,
                new HttpEntity<>(BarobillSoap.buildEnvelopeXml(method, innerXml), headers), byte[].class).getBody();
        if (response == null) return Optional.empty();
        return Optional.of(new String(response, StandardCharsets.UTF_8));
    }

    private Map<String, String> substitutions(AlimtalkVariables v) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("#{업체명}", nullToEmpty(v.companyName()));
        values.put("#{받는분}", nullToEmpty(v.recipientName()));
        values.put("#{주문번호}", nullToEmpty(v.orderNumber()));
        values.put("#{장소}", nullToEmpty(v.placeName()));
        values.put("#{사진링크}", nullToEmpty(v.photoToken()));
        return values;
    }

    private String apply(String text, Map<String, String> values) {
        String result = nullToEmpty(text);
        for (Map.Entry<String, String> e : values.entrySet()) {
            result = result.replace(e.getKey(), e.getValue());
        }
        return result;
    }

    private boolean containsPlaceholder(String text) {
        return text != null && text.contains("#{");
    }

    private AlimtalkSendResult fail(String reason) {
        return new AlimtalkSendResult(false, false, reason);
    }

    private String describe(Exception e) {
        if (e instanceof RestClientResponseException re) {
            return "바로빌 응답 오류 (HTTP " + re.getStatusCode().value() + ")";
        }
        return "바로빌 호출 실패: " + e.getClass().getSimpleName();
    }

    private String digits(String s) {
        return s == null ? "" : s.replaceAll("[^0-9]", "");
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
