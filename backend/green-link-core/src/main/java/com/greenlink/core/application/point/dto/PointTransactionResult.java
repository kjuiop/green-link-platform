package com.greenlink.core.application.point.dto;

public record PointTransactionResult(
        Long id,
        String type,
        long amount,
        long balanceAfter,
        String memo,
        String memberLoginId,
        String createdAt
) {}
