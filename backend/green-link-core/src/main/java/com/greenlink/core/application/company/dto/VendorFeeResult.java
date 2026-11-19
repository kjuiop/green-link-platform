package com.greenlink.core.application.company.dto;

import com.greenlink.core.domain.company.model.Company;

public record VendorFeeResult(
        Long companyId,
        String companyName,
        String type,
        String businessNo,
        long vendorFee
) {
    public static VendorFeeResult from(Company c) {
        return new VendorFeeResult(c.getId(), c.getCompanyName(), c.getType().name(),
                c.getBusinessNo(), c.getVendorFee());
    }
}
