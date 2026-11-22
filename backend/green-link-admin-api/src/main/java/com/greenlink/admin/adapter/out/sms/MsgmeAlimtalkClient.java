package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.model.AlimtalkRequest;
import com.greenlink.core.domain.sms.model.AlimtalkRequest.AlimtalkVariables;
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
    public AlimtalkSendResult send(AlimtalkRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("api_key", request.apiKey());
        body.add("senderkey", request.senderKey());
        body.add("template_code", request.templateCode());
        body.add("variable", toVariable(request.variables()));
        body.add("callback", request.callback() != null ? request.callback().replace("-", "") : "");
        body.add("dstaddr", request.receiver() != null ? request.receiver().replace("-", "") : "");
        body.add("send_reserve", "0");
        body.add("next_type", "1");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(URL, entity, Map.class);
            if (response.getBody() == null) return new AlimtalkSendResult(false, false, "EMPTY_RESPONSE");
            String result = String.valueOf(response.getBody().get("result"));
            boolean success = "100".equals(result);
            return new AlimtalkSendResult(success, false, result);
        } catch (Exception e) {
            return new AlimtalkSendResult(false, false, e.getMessage());
        }
    }

    static String toVariable(AlimtalkVariables v) {
        return String.join("|", clean(v.companyName()), clean(v.recipientName()),
                clean(v.orderNumber()), clean(v.placeName()), clean(v.photoToken()));
    }

    private static String clean(String val) {
        return val != null ? val.replace("|", "") : "";
    }
}
