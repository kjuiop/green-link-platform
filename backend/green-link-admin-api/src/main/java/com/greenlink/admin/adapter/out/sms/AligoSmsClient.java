package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.domain.sms.port.SmsClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AligoSmsClient implements SmsClient {

    private final AligoProperties props;
    private final RestTemplate restTemplate;

    public AligoSmsClient(AligoProperties props, RestTemplate smsRestTemplate) {
        this.props = props;
        this.restTemplate = smsRestTemplate;
    }

    @Override
    public SmsResult send(String receiver, String msg, String imageUrl, String sender, String rdate, String rtime, String title) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("key", props.key());
            body.add("userid", props.userid());
            // DB 설정 발신번호 우선, 없으면 application.yml 폴백
            body.add("sender", (sender != null && !sender.isBlank()) ? sender.replaceAll("[^0-9]", "") : props.sender());
            body.add("receiver", receiver.replaceAll("[^0-9]", ""));
            body.add("msg", msg);
            if (rdate != null && !rdate.isBlank()) {
                body.add("rdate", rdate);
                body.add("rtime", (rtime != null && !rtime.isBlank()) ? rtime : "0000");
            }

            String msgType;
            if (imageUrl != null && !imageUrl.isBlank()) {
                byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
                if (imageBytes != null) {
                    msgType = "MMS";
                    body.add("msg_type", msgType);
                    ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                        @Override
                        public String getFilename() {
                            return "image.jpg";
                        }
                    };
                    body.add("image", imageResource);
                } else {
                    msgType = "SMS";
                    body.add("msg_type", msgType);
                }
            } else {
                boolean isLong = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 90;
                msgType = isLong ? "LMS" : "SMS";
                body.add("msg_type", msgType);
            }
            if (title != null && !title.isBlank() && (msgType.equals("LMS") || msgType.equals("MMS"))) {
                body.add("title", title);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = restTemplate.postForObject(props.url(), request, Map.class);
            if (responseBody == null) {
                return new SmsResult(false, null, "응답 없음");
            }

            String resultCode = String.valueOf(responseBody.get("result_code"));
            int code = Integer.parseInt(resultCode);
            if (code > 0) {
                String msgId = String.valueOf(responseBody.get("msg_id"));
                return new SmsResult(true, msgId, null);
            } else {
                String message = String.valueOf(responseBody.get("message"));
                return SmsResult.rejected(message);
            }
        } catch (Exception e) {
            return new SmsResult(false, null, e.getMessage());
        }
    }
}
