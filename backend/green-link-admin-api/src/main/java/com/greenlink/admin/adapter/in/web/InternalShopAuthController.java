package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.BatchShopAccountsRequest;
import com.greenlink.admin.adapter.in.web.dto.ShopBusinessNoRequest;
import com.greenlink.admin.adapter.in.web.dto.ShopSignupRequest;
import com.greenlink.admin.adapter.in.web.dto.VerifyShopAccountRequest;
import com.greenlink.core.application.auth.dto.AccountSummary;
import com.greenlink.core.application.auth.dto.ShopSignupResult;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.ShopSignupUseCase;
import com.greenlink.core.application.auth.usecase.VerifyAdminAccountUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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
    private final byte[] internalSecret;

    public InternalShopAuthController(VerifyAdminAccountUseCase useCase,
                                      ShopSignupUseCase signupUseCase,
                                      @Value("${shop.internal.secret:}") String internalSecret) {
        this.useCase = useCase;
        this.signupUseCase = signupUseCase;
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

    private void requireSecret(String provided) {
        if (internalSecret.length == 0 || provided == null
                || !MessageDigest.isEqual(internalSecret, provided.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(AuthErrorCode.INTERNAL_ACCESS_DENIED);
        }
    }
}
