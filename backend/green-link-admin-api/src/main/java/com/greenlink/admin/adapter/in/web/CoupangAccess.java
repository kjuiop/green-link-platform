package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CoupangAccess {

    /** 발주사 계정이면 본인 회사 ID, 슈퍼관리자면 null(제한 없음). */
    public Long callerCompanyId(Authentication auth) {
        boolean superAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (superAdmin) return null;
        if (auth.getDetails() instanceof Long companyId) return companyId;
        throw new BusinessException(CoupangErrorCode.FORBIDDEN_COMPANY);
    }

    /** 연동 설정·수집처럼 반드시 특정 발주사가 필요한 요청의 대상 회사를 결정한다. */
    public Long targetCompanyId(Authentication auth, Long requestedCompanyId) {
        Long caller = callerCompanyId(auth);
        if (caller != null) return caller;
        if (requestedCompanyId == null) throw new BusinessException(CoupangErrorCode.COMPANY_REQUIRED);
        return requestedCompanyId;
    }
}
