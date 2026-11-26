package com.greenlink.core.domain.onurimapplication.model;

import java.util.List;

public record OnurimApplicationInfo(
    String companyPhone,
    String address,
    String detailAddress,
    String taxInvoiceEmail,
    String memo,
    String faxNumber,
    String bankName,
    String accountNumber,
    String accountHolder,
    List<OnurimApplicationRegion> regions
) {}
