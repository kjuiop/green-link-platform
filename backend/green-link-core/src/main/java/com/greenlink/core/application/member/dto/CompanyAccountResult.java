package com.greenlink.core.application.member.dto;

public record CompanyAccountResult(
        String bankName,
        String accountNumber,
        String accountHolder
) {}
