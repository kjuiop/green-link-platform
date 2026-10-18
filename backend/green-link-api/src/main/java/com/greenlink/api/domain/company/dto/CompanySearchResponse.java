package com.greenlink.api.domain.company.dto;

import com.greenlink.core.domain.company.entity.Company;

public record CompanySearchResponse(
    String businessNo,
    String companyName,
    String ceoName,
    String phone,
    String address,
    String type
) {
    public static CompanySearchResponse from(Company c) {
        return new CompanySearchResponse(
            c.getBusinessNo(),
            c.getCompanyName(),
            c.getCeoName(),
            c.getPhone(),
            c.getAddress(),
            c.getType().name()
        );
    }
}
