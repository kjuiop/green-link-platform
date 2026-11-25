package com.greenlink.shop.adapter.out.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

@Component
public class OnurimIdentityVerifier implements IdentityVerifier {

    private static final Logger log = LoggerFactory.getLogger(OnurimIdentityVerifier.class);
    private static final String SECRET_HEADER = "X-Internal-Secret";
    private static final int BATCH_LIMIT = 100;
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final RestClient restClient;
    private final String secret;

    @Autowired
    public OnurimIdentityVerifier(@Value("${shop.identity.base-url:}") String baseUrl,
                                  @Value("${shop.identity.secret:}") String secret) {
        this(buildClient(baseUrl), secret);
    }

    OnurimIdentityVerifier(RestClient restClient, String secret) {
        this.restClient = restClient;
        this.secret = secret;
    }

    private static RestClient buildClient(String baseUrl) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        factory.setReadTimeout(TIMEOUT);
        RestClient.Builder builder = RestClient.builder().requestFactory(factory);
        if (baseUrl != null && !baseUrl.isBlank()) {
            builder.baseUrl(baseUrl);
        }
        return builder.build();
    }

    @Override
    public VerifiedIdentity verify(String loginId, String password) {
        return call(() -> restClient.post()
            .uri("/internal/shop-auth/verify")
            .header(SECRET_HEADER, secret)
            .body(new VerifyBody(loginId, password))
            .retrieve()
            .body(AccountBody.class));
    }

    @Override
    public VerifiedIdentity getAccount(Long adminId) {
        return call(() -> restClient.get()
            .uri("/internal/shop-auth/accounts/{adminId}", adminId)
            .header(SECRET_HEADER, secret)
            .retrieve()
            .body(AccountBody.class));
    }

    @Override
    public List<AccountProfile> getAccounts(Collection<Long> adminIds) {
        if (adminIds == null || adminIds.isEmpty()) {
            return List.of();
        }
        List<AccountProfile> result = new java.util.ArrayList<>();
        List<Long> ids = List.copyOf(adminIds);
        for (int from = 0; from < ids.size(); from += BATCH_LIMIT) {
            List<Long> chunk = ids.subList(from, Math.min(from + BATCH_LIMIT, ids.size()));
            SummaryBody[] body = execute(() -> restClient.post()
                .uri("/internal/shop-auth/accounts/batch")
                .header(SECRET_HEADER, secret)
                .body(new BatchBody(chunk))
                .retrieve()
                .body(SummaryBody[].class));
            if (body == null) {
                throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
            }
            for (SummaryBody b : body) {
                result.add(new AccountProfile(b.adminId(), b.loginId(), b.name(), b.companyName()));
            }
        }
        return result;
    }

    private VerifiedIdentity call(java.util.function.Supplier<AccountBody> request) {
        AccountBody body = execute(request);
        if (body == null || body.adminId() == null) {
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        return new VerifiedIdentity(body.adminId(), body.loginId(), body.name(), body.companyId(), body.companyName(),
            body.companyOnurimEnabled(), body.companySiteEnabled(), body.adminOnurimEnabled(), body.superAdmin());
    }

    private <T> T execute(java.util.function.Supplier<T> request) {
        if (secret == null || secret.isBlank()) {
            log.error("shop.identity.secret is not configured");
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        try {
            return request.get();
        } catch (RestClientResponseException e) {
            throw mapError(e);
        } catch (ResourceAccessException e) {
            log.error("온우림 계정 서버 호출 실패: {}", e.getMessage());
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("온우림 계정 서버 호출 설정 오류: {}", e.getMessage());
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
    }

    private ShopBusinessException mapError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        if (status == 401) {
            return new ShopBusinessException(ShopErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        if (status == 403 && (body.contains("AUTH_006") || body.contains("AUTH_007"))) {
            return new ShopBusinessException(ShopErrorCode.AUTH_ACCOUNT_INACTIVE);
        }
        log.error("온우림 계정 서버 비정상 응답: status={}", status);
        return new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
    }

    record VerifyBody(String loginId, String password) {}

    record BatchBody(List<Long> adminIds) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AccountBody(Long adminId, String loginId, String name, Long companyId, String companyName,
                       boolean companyOnurimEnabled, boolean companySiteEnabled,
                       boolean adminOnurimEnabled, boolean superAdmin) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SummaryBody(Long adminId, String loginId, String name, Long companyId, String companyName) {}
}
