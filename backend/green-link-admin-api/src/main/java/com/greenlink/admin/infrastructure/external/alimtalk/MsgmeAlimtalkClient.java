package com.greenlink.admin.infrastructure.external.alimtalk;

import com.greenlink.core.domain.sms.port.AlimtalkClient;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class MsgmeAlimtalkClient implements AlimtalkClient {

    private static final String URL = "http://221.139.14.136/APIV2/API/alimtalk_send";
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AlimtalkSendResult send(String apiKey, String senderKey, String templateCode,
                                   String variable, String callback, String dstaddr) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("api_key", apiKey);
        body.add("senderkey", senderKey);
        body.add("template_code", templateCode);
        body.add("variable", variable);
        body.add("callback", callback != null ? callback.replace("-", "") : "");
        body.add("dstaddr", dstaddr != null ? dstaddr.replace("-", "") : "");
        body.add("send_reserve", "0");
        body.add("next_type", "1");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(URL, request, Map.class);
            if (response.getBody() == null) return new AlimtalkSendResult(false, false, "EMPTY_RESPONSE");
            String result = String.valueOf(response.getBody().get("result"));
            boolean success = "100".equals(result);
            return new AlimtalkSendResult(success, false, result);
        } catch (Exception e) {
            return new AlimtalkSendResult(false, false, e.getMessage());
        }
    }
}
