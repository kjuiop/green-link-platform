package com.greenlink.core.domain.company.model;

public record CompanyRegion(Long id, Long companyId, String sido, String sigungu) {

    public static CompanyRegion of(Long companyId, String sido, String sigungu) {
        return new CompanyRegion(null, companyId, sido, sigungu);
    }
}
