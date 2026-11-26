package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.BatchShopAccountsRequest;
import com.greenlink.admin.adapter.in.web.dto.BatchShopCompaniesRequest;
import com.greenlink.admin.adapter.in.web.dto.OnurimApplicationApplyRequest;
import com.greenlink.admin.adapter.in.web.dto.ShopAccessGrantRequest;
import com.greenlink.admin.adapter.in.web.dto.ShopBusinessNoRequest;
import com.greenlink.admin.adapter.in.web.dto.ShopManagedAccountRequest;
import com.greenlink.admin.adapter.in.web.dto.ShopPasswordSetupRequest;
import com.greenlink.admin.adapter.in.web.dto.ShopSignupRequest;
import com.greenlink.admin.adapter.in.web.dto.VerifyShopAccountRequest;
import com.greenlink.core.application.auth.dto.AccountSummary;
import com.greenlink.core.application.auth.dto.CompanySummary;
import com.greenlink.core.application.auth.dto.ShopSignupResult;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.ShopAccessUseCase;
import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.usecase.ApplyOnurimUseCase;
import com.greenlink.core.application.auth.usecase.ShopManagedAccountUseCase;
import com.greenlink.core.application.auth.usecase.ShopSignupUseCase;
import com.greenlink.core.application.auth.usecase.VerifyAdminAccountUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/shop-auth")
public class InternalShopAuthController {

    private static final String SECRET_HEADER = "X-Internal-Secret";

    private final VerifyAdminAccountUseCase useCase;
    private final ShopSignupUseCase signupUseCase;
    private final ShopAccessUseCase accessUseCase;
    private final ShopManagedAccountUseCase managedAccountUseCase;
    private final ApplyOnurimUseCase onurimApplicationUseCase;
    private final byte[] internalSecret;

    public InternalShopAuthController(VerifyAdminAccountUseCase useCase,
                                      ShopSignupUseCase signupUseCase,
                                      ShopAccessUseCase accessUseCase,
                                      ShopManagedAccountUseCase managedAccountUseCase,
                                      ApplyOnurimUseCase onurimApplicationUseCase,
                                      @Value("${shop.internal.secret:}") String internalSecret) {
        this.useCase = useCase;
        this.signupUseCase = signupUseCase;
        this.accessUseCase = accessUseCase;
        this.managedAccountUseCase = managedAccountUseCase;
        this.onurimApplicationUseCase = onurimApplicationUseCase;
        this.internalSecret = internalSecret.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/verify")
    public VerifiedAccount verify(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                  @RequestBody @Valid VerifyShopAccountRequest req) {
        requireSecret(secret);
        return useCase.verify(req.loginId(), req.password());
    }

    @GetMapping("/accounts/{adminId}")
    public VerifiedAccount getAccount(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                      @PathVariable Long adminId) {
        requireSecret(secret);
        return useCase.getActiveAccount(adminId);
    }

    @PostMapping("/accounts/batch")
    public List<AccountSummary> getAccounts(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                            @RequestBody @Valid BatchShopAccountsRequest req) {
        requireSecret(secret);
        return useCase.getAccountSummaries(req.adminIds());
    }

    @PostMapping("/signup")
    public ShopSignupResult signup(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                   @RequestBody @Valid ShopSignupRequest req) {
        requireSecret(secret);
        return signupUseCase.signup(req.toCommand());
    }

    @PostMapping("/business-numbers/exists")
    public Map<String, Boolean> businessNoExists(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                                 @RequestBody @Valid ShopBusinessNoRequest req) {
        requireSecret(secret);
        return Map.of("exists", signupUseCase.existsBusinessNo(req.businessNo()));
    }

    @PostMapping("/access-grants")
    public VerifiedAccount grantAccess(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                       @RequestBody @Valid ShopAccessGrantRequest req) {
        requireSecret(secret);
        return accessUseCase.grant(req.adminId(), req.companyId());
    }

    @PostMapping("/companies/batch")
    public List<CompanySummary> getCompanies(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                             @RequestBody @Valid BatchShopCompaniesRequest req) {
        requireSecret(secret);
        return accessUseCase.getCompanySummaries(req.companyIds());
    }

    @PostMapping("/managed-accounts")
    public VerifiedAccount createManagedAccount(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                                @RequestBody @Valid ShopManagedAccountRequest req) {
        requireSecret(secret);
        return managedAccountUseCase.create(req.toCommand());
    }

    @PostMapping("/password-setup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setupPassword(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                              @RequestBody @Valid ShopPasswordSetupRequest req) {
        requireSecret(secret);
        managedAccountUseCase.completePasswordSetup(req.loginId(), req.phone(), req.newPassword());
    }

    @PostMapping("/accounts/{adminId}/password-setup-reissue")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reissuePasswordSetup(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                     @PathVariable Long adminId) {
        requireSecret(secret);
        managedAccountUseCase.reissuePasswordSetup(adminId);
    }

    @PostMapping("/onurim-applications")
    public OnurimApplicationResult applyOnurim(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                               @RequestBody @Valid OnurimApplicationApplyRequest req) {
        requireSecret(secret);
        return onurimApplicationUseCase.apply(req.adminId(), req.companyId(), req.desiredType(), req.toInfo());
    }

    @GetMapping("/onurim-applications/latest")
    public ResponseEntity<OnurimApplicationResult> latestOnurimApplication(
            @RequestHeader(value = SECRET_HEADER, required = false) String secret,
            @RequestParam Long companyId) {
        requireSecret(secret);
        return onurimApplicationUseCase.getLatest(companyId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private void requireSecret(String provided) {
        if (internalSecret.length == 0 || provided == null
                || !MessageDigest.isEqual(internalSecret, provided.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(AuthErrorCode.INTERNAL_ACCESS_DENIED);
        }
    }
}
