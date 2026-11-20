package com.greenlink.core.application.fax.dto;

import com.greenlink.core.domain.company.model.Company;

public record FaxVendorResult(
        Long companyId,
        String companyName,
        String businessNo,
        String faxNumber,
        boolean faxEnabled
) {
    public static FaxVendorResult from(Company c) {
        return new FaxVendorResult(c.getId(), c.getCompanyName(), c.getBusinessNo(), c.getFaxNumber(), c.isFaxEnabled());
    }
}
