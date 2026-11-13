package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.UpdateBusinessStatusRequest;
import com.greenlink.core.application.member.dto.CompanyAccountResult;
import com.greenlink.core.application.member.dto.MemberSummaryResult;
import com.greenlink.core.application.member.usecase.GetMemberSummaryUseCase;
import com.greenlink.core.application.member.usecase.GetMyAccountUseCase;
import com.greenlink.core.application.member.usecase.UpdateBusinessStatusUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final GetMemberSummaryUseCase getMemberSummaryUseCase;
    private final GetMyAccountUseCase getMyAccountUseCase;
    private final UpdateBusinessStatusUseCase updateBusinessStatusUseCase;

    public MemberController(GetMemberSummaryUseCase getMemberSummaryUseCase,
                            GetMyAccountUseCase getMyAccountUseCase,
                            UpdateBusinessStatusUseCase updateBusinessStatusUseCase) {
        this.getMemberSummaryUseCase = getMemberSummaryUseCase;
        this.getMyAccountUseCase = getMyAccountUseCase;
        this.updateBusinessStatusUseCase = updateBusinessStatusUseCase;
    }

    @GetMapping("/summary")
    public ResponseEntity<MemberSummaryResult> getSummary(Authentication authentication) {
        Long companyId = null;
        if (authentication instanceof UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long cid) {
            companyId = cid;
        }
        if (companyId == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(getMemberSummaryUseCase.getMemberSummary(companyId));
    }

    @GetMapping("/account")
    public ResponseEntity<CompanyAccountResult> getMyAccount(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(getMyAccountUseCase.getMyAccount(companyId));
    }

    @PatchMapping("/business-status")
    public ResponseEntity<Void> updateBusinessStatus(
            @RequestBody @Valid UpdateBusinessStatusRequest req,
            Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        updateBusinessStatusUseCase.updateBusinessStatus(companyId, req.status());
        return ResponseEntity.noContent().build();
    }
}
