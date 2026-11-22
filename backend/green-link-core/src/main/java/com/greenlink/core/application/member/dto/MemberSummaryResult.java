package com.greenlink.core.application.member.dto;

public record MemberSummaryResult(
        String companyType,
        long orderingAmount,
        long vendorAmount,
        long pointBalance
) {}
