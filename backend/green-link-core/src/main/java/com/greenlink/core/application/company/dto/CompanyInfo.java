package com.greenlink.core.application.company.dto;

import com.greenlink.core.domain.company.model.Company;

public record CompanyInfo(
    String businessNo,
    String companyName,
    String ceoName,
    String phone,
    String address,
    String type
) {
    public static CompanyInfo from(Company c) {
        return new CompanyInfo(
            c.getBusinessNo(), c.getCompanyName(), c.getCeoName(),
            c.getPhone(), c.getAddress(), c.getType().name()
        );
    }
}
