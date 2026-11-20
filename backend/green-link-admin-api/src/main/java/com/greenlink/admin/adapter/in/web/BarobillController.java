package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.sms.dto.BarobillQueryResult;
import com.greenlink.core.application.sms.usecase.BarobillQueryUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.sms.SmsErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sms/barobill")
public class BarobillController {

    private final BarobillQueryUseCase queryUseCase;

    public BarobillController(BarobillQueryUseCase queryUseCase) {
        this.queryUseCase = queryUseCase;
    }

    @GetMapping("/sender-check")
    public ResponseEntity<BarobillQueryResult> checkSender(Authentication auth,
                                                           @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(queryUseCase.checkSenderNumber(resolveCompanyId(auth, companyId)));
    }

    @GetMapping("/send-state")
    public ResponseEntity<BarobillQueryResult> sendState(Authentication auth,
                                                         @RequestParam String sendKey,
                                                         @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(queryUseCase.getSendState(resolveCompanyId(auth, companyId), sendKey));
    }

    /** 슈퍼관리자는 요청한 업체(없으면 운영자 기본 계정), 발주사는 항상 본인 업체. */
    private Long resolveCompanyId(Authentication auth, Long requested) {
        boolean superAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (superAdmin) return requested;
        if (auth.getDetails() instanceof Long companyId) return companyId;
        throw new BusinessException(SmsErrorCode.BAROBILL_FORBIDDEN_COMPANY);
    }
}
