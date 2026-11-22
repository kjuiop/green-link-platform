package com.greenlink.core.application.point.dto;

public record PointTransactionResult(
        Long id,
        String type,
        long amount,
        long balanceAfter,
        String memo,
        String memberLoginId,
        Long orderId,
        String createdAt,
        String taxInvoiceStatus,
        String taxInvoiceFailReason
) {
    public PointTransactionResult(Long id, String type, long amount, long balanceAfter, String memo,
                                  String memberLoginId, Long orderId, String createdAt) {
        this(id, type, amount, balanceAfter, memo, memberLoginId, orderId, createdAt, null, null);
    }

    public PointTransactionResult withTaxInvoice(String status, String failReason) {
        return new PointTransactionResult(id, type, amount, balanceAfter, memo, memberLoginId, orderId,
                createdAt, status, failReason);
    }
}
