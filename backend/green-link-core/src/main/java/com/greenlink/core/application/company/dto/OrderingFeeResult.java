package com.greenlink.core.application.company.dto;

import com.greenlink.core.domain.company.model.Company;

public record OrderingFeeResult(
        Long companyId,
        String companyName,
        String type,
        String businessNo,
        String orderingFeeDiscountType,
        long orderingFeeDiscountValue
) {
    public static OrderingFeeResult from(Company c) {
        return new OrderingFeeResult(
                c.getId(), c.getCompanyName(), c.getType().name(), c.getBusinessNo(),
                c.getOrderingFeeDiscountType().name(), c.getOrderingFeeDiscountValue());
    }
}
