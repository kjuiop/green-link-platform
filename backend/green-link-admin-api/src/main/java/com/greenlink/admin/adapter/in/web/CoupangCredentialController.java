package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.coupang.dto.CoupangCredentialResult;
import com.greenlink.core.application.coupang.dto.CoupangSyncLogResult;
import com.greenlink.core.application.coupang.dto.CoupangTestResult;
import com.greenlink.core.application.coupang.dto.SaveCoupangCredentialCommand;
import com.greenlink.core.application.coupang.usecase.ManageCoupangCredentialUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupang")
public class CoupangCredentialController {

    private final ManageCoupangCredentialUseCase credentialUseCase;
    private final CoupangAccess access;

    public CoupangCredentialController(ManageCoupangCredentialUseCase credentialUseCase, CoupangAccess access) {
        this.credentialUseCase = credentialUseCase;
        this.access = access;
    }

    public record SaveRequest(Long companyId, String vendorCode, String accessKey, String secretKey,
                              LocalDate keyIssuedAt, boolean enabled) {}

    @GetMapping("/credentials")
    public ResponseEntity<CoupangCredentialResult> get(Authentication auth,
                                                       @RequestParam(required = false) Long companyId) {
        return credentialUseCase.get(access.targetCompanyId(auth, companyId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/credentials")
    public ResponseEntity<CoupangCredentialResult> save(Authentication auth, @RequestBody SaveRequest req) {
        Long companyId = access.targetCompanyId(auth, req.companyId());
        return ResponseEntity.ok(credentialUseCase.save(new SaveCoupangCredentialCommand(
                companyId, req.vendorCode(), req.accessKey(), req.secretKey(), req.keyIssuedAt(), req.enabled())));
    }

    @PostMapping("/credentials/test")
    public ResponseEntity<CoupangTestResult> test(Authentication auth,
                                                  @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(credentialUseCase.test(access.targetCompanyId(auth, companyId)));
    }

    @GetMapping("/sync-logs")
    public ResponseEntity<List<CoupangSyncLogResult>> syncLogs(Authentication auth,
                                                               @RequestParam(required = false) Long companyId,
                                                               @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(credentialUseCase.recentSyncLogs(access.targetCompanyId(auth, companyId), limit));
    }
}
