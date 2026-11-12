package com.greenlink.core.application.vendor.dto;

import java.util.List;

public record VendorResult(
    Long id,
    String companyName,
    String businessNo,
    String faxNumber,
    String companyPhone,
    String managerName,
    String phone,
    String email,
    String address,
    String detailAddress,
    String loginId,
    String status,
    String createdAt,
    String companyType,
    List<RegionDto> regions,
    List<String> capabilities,
    String businessRegUrl,
    String bankBookUrl
) {
    public record RegionDto(String sido, String sigungu) {}
}
