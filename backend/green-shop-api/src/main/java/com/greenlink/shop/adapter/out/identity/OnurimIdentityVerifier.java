package com.greenlink.shop.adapter.out.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.greenlink.shopcore.application.access.port.CompanyAccessGranter;
import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.dto.ManagedAccountCommand;
import com.greenlink.shopcore.application.auth.dto.RegisteredAccount;
import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityRegistrar;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationStatus;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand;
import com.greenlink.shopcore.application.onurim.dto.OnurimCompanyType;
import com.greenlink.shopcore.application.onurim.dto.OnurimRegion;
import com.greenlink.shopcore.application.onurim.port.OnurimApplicationGateway;
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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OnurimIdentityVerifier implements IdentityVerifier, IdentityRegistrar, CompanyAccessGranter, OnurimApplicationGateway {

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
                result.add(new AccountProfile(b.adminId(), b.loginId(), b.name(), b.companyName(), Boolean.TRUE.equals(b.passwordSetupPending())));
            }
        }
        return result;
    }

    @Override
    public Map<Long, String> getCompanyNames(Collection<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new HashMap<>();
        List<Long> ids = List.copyOf(companyIds);
        for (int from = 0; from < ids.size(); from += BATCH_LIMIT) {
            List<Long> chunk = ids.subList(from, Math.min(from + BATCH_LIMIT, ids.size()));
            CompanyBody[] body = execute(() -> restClient.post()
                .uri("/internal/shop-auth/companies/batch")
                .header(SECRET_HEADER, secret)
                .body(new CompanyBatchBody(chunk))
                .retrieve()
                .body(CompanyBody[].class));
            if (body == null) {
                throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
            }
            for (CompanyBody b : body) {
                result.put(b.companyId(), b.companyName());
            }
        }
        return result;
    }

    @Override
    public void grant(Long adminId, Long companyId) {
        AccountBody body = executeWith(() -> restClient.post()
            .uri("/internal/shop-auth/access-grants")
            .header(SECRET_HEADER, secret)
            .body(new AccessGrantBody(adminId, companyId))
            .retrieve()
            .body(AccountBody.class), this::mapGrantError);
        if (body == null || body.adminId() == null) {
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
    }

    @Override
    public RegisteredAccount register(SignupCommand command) {
        SignupBody body = executeWith(() -> restClient.post()
            .uri("/internal/shop-auth/signup")
            .header(SECRET_HEADER, secret)
            .body(command)
            .retrieve()
            .body(SignupBody.class), this::mapSignupError);
        if (body == null || body.account() == null || body.account().adminId() == null) {
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        return new RegisteredAccount(toIdentity(body.account()), body.targetCompanyId(), body.founder());
    }

    @Override
    public boolean businessNoExists(String businessNo) {
        ExistsBody body = executeWith(() -> restClient.post()
            .uri("/internal/shop-auth/business-numbers/exists")
            .header(SECRET_HEADER, secret)
            .body(new BusinessNoBody(businessNo))
            .retrieve()
            .body(ExistsBody.class), this::mapSignupError);
        if (body == null) {
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        return body.exists();
    }

    @Override
    public VerifiedIdentity createManagedAccount(ManagedAccountCommand command) {
        AccountBody body = executeWith(() -> restClient.post()
            .uri("/internal/shop-auth/managed-accounts")
            .header(SECRET_HEADER, secret)
            .body(command)
            .retrieve()
            .body(AccountBody.class), this::mapManagedAccountError);
        if (body == null || body.adminId() == null) {
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        return toIdentity(body);
    }

    @Override
    public void completePasswordSetup(String loginId, String phone, String newPassword) {
        executeWith(() -> restClient.post()
            .uri("/internal/shop-auth/password-setup")
            .header(SECRET_HEADER, secret)
            .body(new PasswordSetupBody(loginId, phone, newPassword))
            .retrieve()
            .toBodilessEntity(), this::mapPasswordSetupError);
    }

    @Override
    public void reissuePasswordSetup(Long adminId) {
        executeWith(() -> restClient.post()
            .uri("/internal/shop-auth/accounts/{adminId}/password-setup-reissue", adminId)
            .header(SECRET_HEADER, secret)
            .retrieve()
            .toBodilessEntity(), this::mapReissueError);
    }

    @Override
    public OnurimApplicationView apply(Long adminId, Long companyId, OnurimApplyCommand command) {
        ApplicationBody body = executeWith(() -> restClient.post()
            .uri("/internal/shop-auth/onurim-applications")
            .header(SECRET_HEADER, secret)
            .body(new ApplyBody(adminId, companyId, command))
            .retrieve()
            .body(ApplicationBody.class), this::mapApplicationError);
        if (body == null || body.id() == null) {
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        return body.toView();
    }

    @Override
    public Optional<OnurimApplicationView> findLatest(Long companyId) {
        ApplicationBody body = execute(() -> restClient.get()
            .uri("/internal/shop-auth/onurim-applications/latest?companyId={companyId}", companyId)
            .header(SECRET_HEADER, secret)
            .retrieve()
            .body(ApplicationBody.class));
        return body == null || body.id() == null ? Optional.empty() : Optional.of(body.toView());
    }

    private VerifiedIdentity call(java.util.function.Supplier<AccountBody> request) {
        AccountBody body = execute(request);
        if (body == null || body.adminId() == null) {
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        return toIdentity(body);
    }

    private VerifiedIdentity toIdentity(AccountBody body) {
        return new VerifiedIdentity(body.adminId(), body.loginId(), body.name(), body.companyId(), body.companyName(),
            body.companyOnurimEnabled(), body.companySiteEnabled(), body.adminOnurimEnabled(), body.superAdmin());
    }

    private <T> T execute(java.util.function.Supplier<T> request) {
        return executeWith(request, this::mapError);
    }

    private <T> T executeWith(java.util.function.Supplier<T> request,
                              java.util.function.Function<RestClientResponseException, ShopBusinessException> errorMapper) {
        if (secret == null || secret.isBlank()) {
            log.error("shop.identity.secret is not configured");
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
        try {
            return request.get();
        } catch (RestClientResponseException e) {
            throw errorMapper.apply(e);
        } catch (ResourceAccessException e) {
            log.error("온우림 계정 서버 호출 실패: {}", e.getMessage());
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("온우림 계정 서버 호출 설정 오류: {}", e.getMessage());
            throw new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        }
    }

    private ShopBusinessException mapSignupError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        if (status == 409 && body.contains("AUTH_015")) {
            return new ShopBusinessException(ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED);
        }
        if (status == 409 && body.contains("AUTH_016")) {
            return new ShopBusinessException(ShopErrorCode.SIGNUP_BUSINESS_NO_REGISTERED);
        }
        if (status == 400) {
            return new ShopBusinessException(ShopErrorCode.SIGNUP_INVALID_INPUT);
        }
        return mapError(e);
    }

    private ShopBusinessException mapManagedAccountError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        if (status == 409 && body.contains("AUTH_015")) {
            return new ShopBusinessException(ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED);
        }
        if (status == 404 && body.contains("AUTH_021")) {
            return new ShopBusinessException(ShopErrorCode.MEMBER_COMPANY_REQUIRED);
        }
        if (status == 400) {
            return new ShopBusinessException(ShopErrorCode.SIGNUP_INVALID_INPUT);
        }
        return mapError(e);
    }

    private ShopBusinessException mapPasswordSetupError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == 400 || status == 404 || status == 409) {
            return new ShopBusinessException(ShopErrorCode.AUTH_PASSWORD_SETUP_FAILED);
        }
        return mapError(e);
    }

    private ShopBusinessException mapReissueError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        if (status == 409 && body.contains("AUTH_025")) {
            return new ShopBusinessException(ShopErrorCode.AUTH_PASSWORD_SETUP_NOT_PENDING);
        }
        if (status == 404) {
            return new ShopBusinessException(ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
        }
        return mapError(e);
    }

    private ShopBusinessException mapGrantError(RestClientResponseException e) {
        if (e.getStatusCode().value() == 409 && e.getResponseBodyAsString().contains("AUTH_020")) {
            return new ShopBusinessException(ShopErrorCode.ACCESS_COMPANY_MISMATCH);
        }
        if (e.getStatusCode().value() == 404) {
            String body = e.getResponseBodyAsString();
            if (body.contains("AUTH_021") || body.contains("AUTH_022")) {
                return new ShopBusinessException(ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
            }
        }
        return mapError(e);
    }

    private ShopBusinessException mapApplicationError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        if (status == 409 && body.contains("ONURIM_APP_002")) {
            return new ShopBusinessException(ShopErrorCode.ONURIM_ALREADY_ENABLED);
        }
        if (status == 409 && body.contains("ONURIM_APP_003")) {
            return new ShopBusinessException(ShopErrorCode.ONURIM_APPLICATION_ALREADY_PENDING);
        }
        if (status == 403 && body.contains("ONURIM_APP_008")) {
            return new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        }
        if (status == 400) {
            return new ShopBusinessException(ShopErrorCode.ONURIM_APPLICATION_INVALID);
        }
        return mapError(e);
    }

    private ShopBusinessException mapError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        if (status == 401) {
            return new ShopBusinessException(ShopErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        if (status == 403 && body.contains("AUTH_023")) {
            return new ShopBusinessException(ShopErrorCode.AUTH_PASSWORD_NOT_SET);
        }
        if (status == 403 && (body.contains("AUTH_006") || body.contains("AUTH_007"))) {
            return new ShopBusinessException(ShopErrorCode.AUTH_ACCOUNT_INACTIVE);
        }
        log.error("온우림 계정 서버 비정상 응답: status={}", status);
        return new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
    }

    record VerifyBody(String loginId, String password) {}

    record BatchBody(List<Long> adminIds) {}

    record PasswordSetupBody(String loginId, String phone, String newPassword) {
        @Override
        public String toString() {
            return "PasswordSetupBody[loginId=" + loginId + ", newPassword=****]";
        }
    }

    record BusinessNoBody(String businessNo) {}

    record ApplyBody(Long adminId, Long companyId, OnurimCompanyType desiredType, String companyPhone,
                     String address, String detailAddress, String taxInvoiceEmail, String memo,
                     String faxNumber, String bankName, String accountNumber, String accountHolder,
                     List<OnurimRegion> regions) {
        ApplyBody(Long adminId, Long companyId, OnurimApplyCommand c) {
            this(adminId, companyId, c.desiredType(), c.companyPhone(), c.address(), c.detailAddress(),
                c.taxInvoiceEmail(), c.memo(), c.faxNumber(), c.bankName(), c.accountNumber(),
                c.accountHolder(), c.regions());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ApplicationBody(Long id, OnurimApplicationStatus status, OnurimCompanyType desiredType,
                           OnurimCompanyType approvedType, String rejectReason,
                           LocalDateTime processedAt, LocalDateTime createdAt) {
        OnurimApplicationView toView() {
            return new OnurimApplicationView(id, status, desiredType, approvedType, rejectReason, processedAt, createdAt);
        }
    }

    record CompanyBatchBody(List<Long> companyIds) {}

    record AccessGrantBody(Long adminId, Long companyId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CompanyBody(Long companyId, String companyName) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExistsBody(boolean exists) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SignupBody(AccountBody account, Long targetCompanyId, boolean founder) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AccountBody(Long adminId, String loginId, String name, Long companyId, String companyName,
                       boolean companyOnurimEnabled, boolean companySiteEnabled,
                       boolean adminOnurimEnabled, boolean superAdmin) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SummaryBody(Long adminId, String loginId, String name, Long companyId, String companyName,
                       Boolean passwordSetupPending) {}
}
