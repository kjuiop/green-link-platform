package com.greenlink.core.application.product.dto;

import java.util.List;

public record VendorSearchResult(
        Long vendorId,
        String companyName,
        String phone,
        String businessStatus,
        List<RegionDto> regions,
        List<String> capabilities,
        long matchedProductCount,
        long minPrice
) {
    public record RegionDto(String sido, String sigungu) {}
}
