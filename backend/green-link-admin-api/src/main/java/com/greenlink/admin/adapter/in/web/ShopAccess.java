package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ShopAccess {

    /** 일반 계정이면 본인 회사 ID, 슈퍼관리자면 null(제한 없음). */
    public Long callerCompanyId(Authentication auth) {
        boolean superAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (superAdmin) return null;
        if (auth.getDetails() instanceof Long companyId) return companyId;
        throw new BusinessException(ShopErrorCode.FORBIDDEN_COMPANY);
    }

    /** 쇼핑몰 설정·상품처럼 반드시 특정 회사가 필요한 요청의 대상 회사를 결정한다. */
    public Long targetCompanyId(Authentication auth, Long requestedCompanyId) {
        Long caller = callerCompanyId(auth);
        if (caller != null) return caller;
        if (requestedCompanyId == null) throw new BusinessException(ShopErrorCode.COMPANY_REQUIRED);
        return requestedCompanyId;
    }

    public Long adminId(Authentication auth) {
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ShopErrorCode.FORBIDDEN_COMPANY);
        }
    }
}
