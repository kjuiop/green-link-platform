package com.greenlink.core.application.onurimapplication.dto;

import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationRegion;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OnurimApplicationResult(
    Long id,
    Long companyId,
    String companyName,
    Long applicantAdminId,
    String applicantName,
    CompanyType desiredType,
    OnurimApplicationStatus status,
    String companyPhone,
    String address,
    String detailAddress,
    String taxInvoiceEmail,
    String memo,
    String faxNumber,
    String bankName,
    String accountNumber,
    String accountHolder,
    List<OnurimApplicationRegion> regions,
    String businessRegUrl,
    String bankBookUrl,
    CompanyType approvedType,
    String rejectReason,
    LocalDateTime processedAt,
    LocalDateTime createdAt
) {}
