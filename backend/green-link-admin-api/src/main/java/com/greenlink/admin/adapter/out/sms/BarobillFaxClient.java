package com.greenlink.admin.adapter.out.sms;

import com.greenlink.admin.adapter.out.fax.BarobillFtpUploader;
import com.greenlink.core.domain.fax.port.FaxGateway;
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

@Component
public class BarobillFaxClient implements FaxGateway {

    private static final Logger log = LoggerFactory.getLogger(BarobillFaxClient.class);

    private final BarobillProperties props;
    private final RestTemplate restTemplate;
    private final BarobillFtpUploader ftpUploader;

    public BarobillFaxClient(BarobillProperties props, RestTemplate smsRestTemplate, BarobillFtpUploader ftpUploader) {
        this.props = props;
        this.restTemplate = smsRestTemplate;
        this.ftpUploader = ftpUploader;
    }

    @Override
    public FaxSendResult send(FaxAccount account, String fileName, byte[] pdf, String toNumber,
                              String receiveName, String refKey) {
        try {
            ftpUploader.upload(account.ftpHost(), account.ftpPort(), account.ftpUsername(), account.ftpPassword(),
                    account.ftpDirectory(), fileName, pdf);
        } catch (Exception e) {
            log.warn("[바로빌 팩스 FTP 업로드 실패] error={}", e.getClass().getSimpleName());
            return new FaxSendResult(false, null, "팩스 파일 업로드 실패: " + e.getClass().getSimpleName());
        }
        try {
            String inner = BarobillSoap.element("CERTKEY", account.apiKey())
                    + BarobillSoap.element("CorpNum", digits(account.corpNum()))
                    + BarobillSoap.element("SenderID", account.userId())
                    + BarobillSoap.element("FileCount", "1")
                    + "<FileNames>" + BarobillSoap.element("string", fileName) + "</FileNames>"
                    + BarobillSoap.element("FromNumber", digits(account.senderNumber()))
                    + BarobillSoap.element("ToNumber", digits(toNumber))
                    + BarobillSoap.element("ReceiveCorp", receiveName)
                    + BarobillSoap.element("ReceiveName", receiveName)
                    + BarobillSoap.element("SendDT", "")
                    + BarobillSoap.element("RefKey", refKey);
            String result = call(account, "SendFaxFromFTPEx", inner)
                    .flatMap(r -> BarobillSoap.parseResult(r, "SendFaxFromFTPEx")).orElse("");
            if (result.isEmpty()) return new FaxSendResult(false, null, "바로빌 응답이 올바르지 않습니다.");
            if (BarobillSoap.isErrorCode(result)) return new FaxSendResult(false, null, "바로빌 오류 (" + result + ")");
            return new FaxSendResult(true, result, null);
        } catch (RestClientResponseException e) {
            return new FaxSendResult(false, null, "바로빌 응답 오류 (HTTP " + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.warn("[바로빌 팩스 발송 실패] error={}", e.getClass().getSimpleName());
            return new FaxSendResult(false, null, "바로빌 호출 실패: " + e.getClass().getSimpleName());
        }
    }

    @Override
    public FaxState getState(FaxAccount account, String sendKey) {
        String inner = BarobillSoap.element("CERTKEY", account.apiKey())
                + BarobillSoap.element("CorpNum", digits(account.corpNum()))
                + BarobillSoap.element("SendKey", sendKey);
        String result = call(account, "GetFaxSendState", inner)
                .flatMap(r -> BarobillSoap.parseResult(r, "GetFaxSendState"))
                .orElseThrow(() -> new IllegalStateException("empty response"));
        if (result.isEmpty()) throw new IllegalStateException("empty result");
        return mapState(Integer.parseInt(result));
    }

    /** 바로빌 팩스 전송상태 코드 해석. 확인된 값(1=전송완료, 음수=오류)만 확정하고 나머지는 진행 중으로 본다. */
    static FaxState mapState(int code) {
        if (code == 1) return FaxState.DELIVERED;
        if (code < 0) return FaxState.FAILED;
        return FaxState.PENDING;
    }

    private java.util.Optional<String> call(FaxAccount account, String method, String innerXml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));
        headers.add("SOAPAction", BarobillSoap.soapAction(method));
        String url = account.serviceUrl() != null && !account.serviceUrl().isBlank() ? account.serviceUrl() : props.faxUrl();
        byte[] response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(BarobillSoap.buildEnvelopeXml(method, innerXml), headers), byte[].class).getBody();
        if (response == null) return java.util.Optional.empty();
        return java.util.Optional.of(new String(response, StandardCharsets.UTF_8));
    }

    private String digits(String s) {
        return s == null ? "" : s.replaceAll("[^0-9]", "");
    }
}
