package com.greenlink.shopcore.application.onurim.dto;

import java.util.List;

public record OnurimApplyCommand(
    OnurimCompanyType desiredType,
    String companyPhone,
    String address,
    String detailAddress,
    String taxInvoiceEmail,
    String memo,
    String faxNumber,
    String bankName,
    String accountNumber,
    String accountHolder,
    List<OnurimRegion> regions
) {}
