package com.greenlink.core.domain.point.model;

public class PointTransaction {

    private Long id;
    private Long companyId;
    private Long memberId;
    private Long adminId;
    private Long orderId;
    private PointTransactionType type;
    private long amount;
    private long balanceAfter;
    private String memo;
    private String createdAt;

    private PointTransaction() {}

    public static PointTransaction charge(Long companyId, Long adminId,
                                           long amount, long balanceAfter, String memo) {
        PointTransaction t = new PointTransaction();
        t.companyId = companyId;
        t.adminId = adminId;
        t.type = PointTransactionType.CHARGE;
        t.amount = amount;
        t.balanceAfter = balanceAfter;
        t.memo = memo;
        return t;
    }

    public static PointTransaction use(Long companyId, Long memberId, Long orderId,
                                        long amount, long balanceAfter, String memo) {
        return orderTransaction(PointTransactionType.USE, companyId, memberId, orderId, amount, balanceAfter, memo);
    }

    public static PointTransaction refund(Long companyId, Long memberId, Long orderId,
                                           long amount, long balanceAfter, String memo) {
        return orderTransaction(PointTransactionType.REFUND, companyId, memberId, orderId, amount, balanceAfter, memo);
    }

    public static PointTransaction settle(Long companyId, Long memberId, Long orderId,
                                           long amount, long balanceAfter, String memo) {
        return orderTransaction(PointTransactionType.SETTLE, companyId, memberId, orderId, amount, balanceAfter, memo);
    }

    public static PointTransaction restore(Long companyId, Long memberId, Long orderId,
                                           long amount, long balanceAfter, String memo) {
        return orderTransaction(PointTransactionType.RESTORE, companyId, memberId, orderId, amount, balanceAfter, memo);
    }

    private static PointTransaction orderTransaction(PointTransactionType type, Long companyId, Long memberId,
                                                      Long orderId, long amount, long balanceAfter, String memo) {
        PointTransaction t = new PointTransaction();
        t.companyId = companyId;
        t.memberId = memberId;
        t.orderId = orderId;
        t.type = type;
        t.amount = amount;
        t.balanceAfter = balanceAfter;
        t.memo = memo;
        return t;
    }

    public static PointTransaction of(Long id, Long companyId, Long memberId, Long adminId, Long orderId,
                                       PointTransactionType type, long amount, long balanceAfter,
                                       String memo, String createdAt) {
        PointTransaction t = new PointTransaction();
        t.id = id;
        t.companyId = companyId;
        t.memberId = memberId;
        t.adminId = adminId;
        t.orderId = orderId;
        t.type = type;
        t.amount = amount;
        t.balanceAfter = balanceAfter;
        t.memo = memo;
        t.createdAt = createdAt;
        return t;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public Long getMemberId() { return memberId; }
    public Long getAdminId() { return adminId; }
    public Long getOrderId() { return orderId; }
    public PointTransactionType getType() { return type; }
    public long getAmount() { return amount; }
    public long getBalanceAfter() { return balanceAfter; }
    public String getMemo() { return memo; }
    public String getCreatedAt() { return createdAt; }
}
