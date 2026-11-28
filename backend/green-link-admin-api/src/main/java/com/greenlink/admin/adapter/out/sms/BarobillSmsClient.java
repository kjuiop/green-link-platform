package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.model.SmsAccount;
import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.domain.sms.port.BarobillGateway;
import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.sms.SmsErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Qualifier("barobill")
public class BarobillSmsClient implements SmsClient, BarobillGateway {

    private static final Logger log = LoggerFactory.getLogger(BarobillSmsClient.class);

    private static final int SMS_MAX_BYTES = 90;
    private static final int LMS_MAX_BYTES = 2000;
    private static final int SUBJECT_MAX_BYTES = 40;

    private final BarobillProperties props;
    private final RestTemplate restTemplate;

    public BarobillSmsClient(BarobillProperties props, RestTemplate smsRestTemplate) {
        this.props = props;
        this.restTemplate = smsRestTemplate;
    }

    @Override
    public SmsResult send(String receiver, String msg, String imageUrl, String sender,
                          String rdate, String rtime, String title) {
        return new SmsResult(false, null, "바로빌: 계정 정보 없이는 발송할 수 없습니다.");
    }

    @Override
    public SmsResult sendWithAccount(SmsAccount account, String receiver, String msg, String imageUrl,
                                     String rdate, String rtime, String title) {
        if (isBlank(account.apiKey()) || isBlank(account.corpNum()) || isBlank(account.userId())
                || isBlank(account.senderNumber())) {
            return new SmsResult(false, null, "바로빌 계정 정보가 설정되지 않았습니다.");
        }
        String message = msg == null ? "" : msg;
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("CERTKEY", account.apiKey());
            params.put("CorpNum", digits(account.corpNum()));
            params.put("SenderID", account.userId());
            params.put("FromNumber", digits(account.senderNumber()));
            params.put("ToName", "");
            params.put("ToNumber", digits(receiver));

            String method;
            if (!isBlank(imageUrl)) {
                byte[] original = restTemplate.getForObject(imageUrl, byte[].class);
                if (original == null || original.length == 0) {
                    return new SmsResult(false, null, "발송할 이미지를 불러오지 못했습니다.");
                }
                byte[] jpeg;
                try {
                    jpeg = BarobillImageConverter.toBarobillJpeg(original);
                } catch (Exception e) {
                    return new SmsResult(false, null, "이미지 변환 실패: " + e.getMessage());
                }
                method = "SendMMSMessage";
                params.put("TXTSubject", BarobillSoap.truncateBytes(title, SUBJECT_MAX_BYTES));
                params.put("TXTMESSAGE", message);
                params.put("ImageFile", Base64.getEncoder().encodeToString(jpeg));
            } else if (BarobillSoap.byteLength(message) <= SMS_MAX_BYTES) {
                method = "SendSMSMessage";
                params.put("Contents", message);
            } else if (BarobillSoap.byteLength(message) <= LMS_MAX_BYTES) {
                method = "SendLMSMessage";
                params.put("Subject", BarobillSoap.truncateBytes(title, SUBJECT_MAX_BYTES));
                params.put("Contents", message);
            } else {
                return new SmsResult(false, null, "메시지가 2,000byte를 초과합니다.");
            }
            params.put("SendDT", toSendDt(rdate, rtime));
            params.put("RefKey", "");

            String result = call(method, params).orElse("");
            if (result.isEmpty()) {
                return new SmsResult(false, null, "바로빌 응답이 올바르지 않습니다.");
            }
            if (BarobillSoap.isErrorCode(result)) {
                return SmsResult.rejected("바로빌 오류 (" + result + ")");
            }
            return new SmsResult(true, result, null);
        } catch (Exception e) {
            String reason = describe(e);
            log.warn("[바로빌 발송 실패] {}", reason, e);
            return new SmsResult(false, null, reason);
        }
    }

    @Override
    public int checkSenderNumber(SmsAccount account) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("CERTKEY", account.apiKey());
        params.put("CorpNum", digits(account.corpNum()));
        params.put("FromNumber", digits(account.senderNumber()));
        return callForInt("CheckSMSFromNumber", params);
    }

    @Override
    public int getSendState(SmsAccount account, String sendKey) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("CERTKEY", account.apiKey());
        params.put("CorpNum", digits(account.corpNum()));
        params.put("SendKey", sendKey);
        return callForInt("GetSMSSendState", params);
    }

    private int callForInt(String method, Map<String, String> params) {
        try {
            String result = call(method, params).orElseThrow(() -> new IllegalStateException("empty"));
            return Integer.parseInt(result);
        } catch (Exception e) {
            log.warn("[바로빌 조회 실패] method={} error={}", method, e.getClass().getSimpleName());
            throw new BusinessException(SmsErrorCode.BAROBILL_UNAVAILABLE);
        }
    }

    private Optional<String> call(String method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));
        headers.add("SOAPAction", BarobillSoap.soapAction(method));
        byte[] response = restTemplate.exchange(props.url(), HttpMethod.POST,
                new HttpEntity<>(BarobillSoap.buildEnvelope(method, params), headers), byte[].class).getBody();
        if (response == null) return Optional.empty();
        return BarobillSoap.parseResult(new String(response, StandardCharsets.UTF_8), method);
    }

    private String describe(Exception e) {
        if (e instanceof RestClientResponseException re) {
            return "바로빌 응답 오류 (HTTP " + re.getStatusCode().value() + " " + re.getStatusText() + ")";
        }
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String detail = root == e ? "" : " (" + root.getClass().getSimpleName() + ": " + abbreviate(root.getMessage()) + ")";
        return "바로빌 호출 실패: " + e.getClass().getSimpleName() + detail;
    }

    private String abbreviate(String s) {
        if (s == null || s.isBlank()) return "-";
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }

    private String toSendDt(String rdate, String rtime) {
        if (isBlank(rdate)) return "";
        String time = (rtime != null && rtime.length() >= 4) ? rtime.substring(0, 4) : "0000";
        return rdate + time + "00";
    }

    private String digits(String s) {
        return s == null ? "" : s.replaceAll("[^0-9]", "");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
