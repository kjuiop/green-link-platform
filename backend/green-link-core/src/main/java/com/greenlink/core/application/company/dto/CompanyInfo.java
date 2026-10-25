package com.greenlink.core.application.company.dto;

import com.greenlink.core.domain.company.model.Company;

public record CompanyInfo(
    Long id,
    String businessNo,
    String companyName,
    String ceoName,
    String phone,
    String address,
    String detailAddress,
    String type,
    String status
) {
    public static CompanyInfo from(Company c) {
        return new CompanyInfo(
            c.getId(), c.getBusinessNo(), c.getCompanyName(), c.getCeoName(),
            c.getPhone(), c.getAddress(), c.getDetailAddress(),
            c.getType().name(),
            c.getStatus().name()
        );
    }
}
