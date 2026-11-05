package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.member.dto.MemberSummaryResult;
import com.greenlink.core.application.member.usecase.GetMemberSummaryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final GetMemberSummaryUseCase getMemberSummaryUseCase;

    public MemberController(GetMemberSummaryUseCase getMemberSummaryUseCase) {
        this.getMemberSummaryUseCase = getMemberSummaryUseCase;
    }

    @GetMapping("/summary")
    public ResponseEntity<MemberSummaryResult> getSummary(Authentication authentication) {
        Long companyId = null;
        if (authentication instanceof UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long cid) {
            companyId = cid;
        }
        return ResponseEntity.ok(getMemberSummaryUseCase.getMemberSummary(companyId));
    }
}
