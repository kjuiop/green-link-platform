package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.domain.sms.port.SmsClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Qualifier("messageme")
public class MessageMeSmsClient implements SmsClient {

    private static final String API_URL = "http://221.139.14.136/APIV2/API/sms_send";

    private final RestTemplate restTemplate;

    public MessageMeSmsClient(RestTemplate smsRestTemplate) {
        this.restTemplate = smsRestTemplate;
    }

    @Override
    public SmsResult send(String receiver, String msg, String imageUrl,
                          String sender, String rdate, String rtime, String title) {
        // company_integrations 없이 직접 호출되는 경우 — 자격증명 없어 발송 불가
        return new SmsResult(false, null, "MessageMe: api_key 미설정 (company_integrations 조회 필요)");
    }

    @Override
    public SmsResult sendWithCredentials(String apiKey, String senderNumber,
                                         String receiver, String msg, String imageUrl,
                                         String sendReserve, String sendReserveDate) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("api_key", apiKey);
            body.add("msg", msg);
            body.add("callback", senderNumber.replaceAll("[^0-9]", ""));
            body.add("dstaddr", receiver.replaceAll("[^0-9]", ""));
            body.add("send_reserve", sendReserve != null ? sendReserve : "0");
            body.add("call_block", "0");

            if (imageUrl != null && !imageUrl.isBlank()) {
                body.add("image", imageUrl);
            }
            if ("1".equals(sendReserve) && sendReserveDate != null) {
                body.add("send_reserve_date", sendReserveDate);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.postForObject(API_URL, request, Map.class);
            if (resp == null) return new SmsResult(false, null, "응답 없음");

            String result = String.valueOf(resp.get("result"));
            if ("100".equals(result)) {
                return new SmsResult(true, null, null);
            }
            return SmsResult.rejected(result);
        } catch (Exception e) {
            return new SmsResult(false, null, e.getMessage());
        }
    }

}
