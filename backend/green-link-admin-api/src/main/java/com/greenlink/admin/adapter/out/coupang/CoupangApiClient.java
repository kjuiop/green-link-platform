package com.greenlink.admin.adapter.out.coupang;

import com.greenlink.core.domain.coupang.model.CoupangCredentialSecret;
import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import com.greenlink.core.domain.coupang.port.CoupangApiException;
import com.greenlink.core.domain.coupang.port.CoupangApiPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CoupangApiClient implements CoupangApiPort {

    private static final Logger log = LoggerFactory.getLogger(CoupangApiClient.class);
    private static final DateTimeFormatter QUERY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final String BASE_PATH = "/v2/providers/openapi/apis/api";
    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 50;

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public CoupangApiClient(@Qualifier("coupangRestTemplate") RestTemplate restTemplate,
                            @Value("${coupang.base-url:https://api-gateway.coupang.com}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // 쿠팡은 시간대가 없으면 400을 반환하며, '+'는 공백으로 해석되므로 %2B로 인코딩해야 한다.
    static String queryTime(LocalDateTime time) {
        return time.format(QUERY_TIME) + "%2B09:00";
    }

    @Override
    public List<CoupangOrderSheet> fetchNewOrderSheets(CoupangCredentialSecret secret,
                                                       LocalDateTime from, LocalDateTime to) {
        String path = BASE_PATH + "/v5/vendors/" + secret.vendorCode() + "/ordersheets";
        List<CoupangOrderSheet> result = new ArrayList<>();
        String nextToken = null;
        for (int page = 0; page < MAX_PAGES; page++) {
            String query = "createdAtFrom=" + queryTime(from)
                    + "&createdAtTo=" + queryTime(to)
                    + "&searchType=timeFrame&status=ACCEPT&maxPerPage=" + PAGE_SIZE
                    + (nextToken == null || nextToken.isBlank() ? "" : "&nextToken=" + URLEncoder.encode(nextToken, StandardCharsets.UTF_8));
            JsonNode body = get(secret, path, query);
            JsonNode data = body.path("data");
            if (data.isArray()) {
                for (JsonNode node : data) {
                    result.add(parseSheet(node));
                }
            }
            nextToken = body.path("nextToken").asString("");
            if (nextToken.isBlank()) break;
        }
        return result;
    }

    @Override
    public Optional<CoupangOrderSheet> fetchOrderSheet(CoupangCredentialSecret secret, long orderId,
                                                       long shipmentBoxId) {
        String path = BASE_PATH + "/v4/vendors/" + secret.vendorCode() + "/" + orderId + "/ordersheets";
        JsonNode body;
        try {
            body = get(secret, path, "");
        } catch (CoupangApiException e) {
            if (e.getHttpStatus() == 404) return Optional.empty();
            throw e;
        }
        JsonNode data = body.path("data");
        List<JsonNode> nodes = new ArrayList<>();
        if (data.isArray()) data.forEach(nodes::add);
        else if (data.isObject()) nodes.add(data);
        return nodes.stream()
                .map(this::parseSheet)
                .filter(s -> s.shipmentBoxId() == shipmentBoxId)
                .findFirst();
    }

    private JsonNode get(CoupangCredentialSecret secret, String path, String query) {
        String authorization = CoupangHmacSigner.authorization("GET", path, query,
                secret.accessKey(), secret.secretKey(), ZonedDateTime.now(ZoneId.of("UTC")));
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.setContentType(MediaType.APPLICATION_JSON);
        URI uri = URI.create(baseUrl + path + (query.isEmpty() ? "" : "?" + query));
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);
            return mapper.readTree(response.getBody() == null ? "{}" : response.getBody());
        } catch (HttpStatusCodeException e) {
            log.warn("쿠팡 API 오류 path={} status={}", path, e.getStatusCode().value());
            throw new CoupangApiException(e.getStatusCode().value(), extractMessage(e.getResponseBodyAsString()), e);
        } catch (ResourceAccessException e) {
            log.warn("쿠팡 API 연결 오류 path={}", path);
            throw new CoupangApiException(0, "연결에 실패했습니다.", e);
        } catch (JacksonException e) {
            log.warn("쿠팡 API 응답 파싱 실패 path={}", path);
            throw new CoupangApiException(0, "응답을 해석하지 못했습니다.", e);
        } catch (RestClientException e) {
            log.warn("쿠팡 API 호출 실패 path={} type={}", path, e.getClass().getSimpleName());
            throw new CoupangApiException(0, "응답을 처리하지 못했습니다.", e);
        }
    }

    private String extractMessage(String body) {
        try {
            String message = mapper.readTree(body).path("message").asString("");
            return message.length() > 200 ? message.substring(0, 200) : message;
        } catch (Exception e) {
            return "";
        }
    }

    CoupangOrderSheet parseSheet(JsonNode n) {
        JsonNode orderer = n.path("orderer");
        JsonNode receiver = n.path("receiver");
        List<CoupangOrderSheet.Item> items = new ArrayList<>();
        long total = 0;
        for (JsonNode item : n.path("orderItems")) {
            int qty = item.path("shippingCount").asInt(0);
            long salesPrice = item.path("salesPrice").asLong(0);
            long orderPrice = item.path("orderPrice").asLong(salesPrice * qty);
            total += orderPrice;
            items.add(new CoupangOrderSheet.Item(item.path("vendorItemName").asString(""), qty, salesPrice));
        }
        return new CoupangOrderSheet(
                n.path("orderId").asLong(0),
                n.path("shipmentBoxId").asLong(0),
                n.path("status").asString(""),
                parseTime(n.path("orderedAt").asString("")),
                orderer.path("name").asString(""),
                firstNonBlank(orderer.path("safeNumber").asString(""), orderer.path("ordererNumber").asString("")),
                receiver.path("name").asString(""),
                firstNonBlank(receiver.path("safeNumber").asString(""), receiver.path("receiverNumber").asString("")),
                receiver.path("addr1").asString(""),
                receiver.path("addr2").asString(""),
                n.path("parcelPrintMessage").asString(""),
                items,
                total,
                n.toString());
    }

    private static LocalDateTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }
}
