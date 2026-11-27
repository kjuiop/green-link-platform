package com.greenlink.core.application.auth;

import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** 관리자 권한(role)과 회사 유형에서 접근 제어용 authority 이름과 업무 capability를 파생한다. */
public final class AdminAuthorities {

    private AdminAuthorities() {}

    /** capability authority(ORDERING/VENDOR)는 파트너·브랜드 관리자에게 회사 유형에서 파생된다. */
    public static List<String> from(AdminRole role, CompanyType companyType) {
        if (role == null) return List.of();
        List<String> out = new ArrayList<>();
        out.add(role.name());
        capabilities(role, companyType).forEach(c -> out.add(c.name()));
        return out;
    }

    /** BOTH 회사는 ORDERING과 VENDOR를 모두 가진다. 슈퍼관리자는 회사 유형 조건을 따지지 않으므로 비어 있다. */
    public static Set<CompanyType> capabilities(AdminRole role, CompanyType companyType) {
        Set<CompanyType> out = EnumSet.noneOf(CompanyType.class);
        boolean companyAdmin = role == AdminRole.PARTNER_ADMIN || role == AdminRole.BRAND_ADMIN;
        if (!companyAdmin || companyType == null) return out;
        if (companyType == CompanyType.ORDERING || companyType == CompanyType.BOTH) out.add(CompanyType.ORDERING);
        if (companyType == CompanyType.VENDOR || companyType == CompanyType.BOTH) out.add(CompanyType.VENDOR);
        return out;
    }
}
