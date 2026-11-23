package com.greenlink.core.domain.point.model;

public class CompanyPoint {

    private Long id;
    private Long companyId;
    private long balance;
    private long depositBalance;

    private CompanyPoint() {}

    public static CompanyPoint init(Long companyId) {
        CompanyPoint p = new CompanyPoint();
        p.companyId = companyId;
        p.balance = 0L;
        p.depositBalance = 0L;
        return p;
    }

    public static CompanyPoint of(Long id, Long companyId, long balance, long depositBalance) {
        CompanyPoint p = new CompanyPoint();
        p.id = id;
        p.companyId = companyId;
        p.balance = balance;
        p.depositBalance = depositBalance;
        return p;
    }

    public void charge(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        try {
            this.balance = Math.addExact(this.balance, amount);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("충전 후 잔액이 허용 범위를 초과합니다.");
        }
    }

    public boolean canHold(long amount) {
        return balance >= amount;
    }

    public void hold(long amount) {
        if (amount < 0) throw new IllegalArgumentException("예치 금액은 0 이상이어야 합니다.");
        if (balance < amount) throw new IllegalStateException("가용 포인트가 부족합니다.");
        this.depositBalance = Math.addExact(this.depositBalance, amount);
        this.balance -= amount;
    }

    public void release(long amount) {
        if (amount < 0) throw new IllegalArgumentException("환불 금액은 0 이상이어야 합니다.");
        if (depositBalance < amount) throw new IllegalStateException("예치금이 부족합니다.");
        this.balance = Math.addExact(this.balance, amount);
        this.depositBalance -= amount;
    }

    public void settle(long amount) {
        if (amount < 0) throw new IllegalArgumentException("정산 금액은 0 이상이어야 합니다.");
        if (depositBalance < amount) throw new IllegalStateException("예치금이 부족합니다.");
        this.depositBalance -= amount;
    }

    public void deduct(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        if (balance < amount) throw new IllegalStateException("가용 포인트가 부족합니다.");
        this.balance -= amount;
    }

    public void restore(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("복구 금액은 0보다 커야 합니다.");
        try {
            this.balance = Math.addExact(this.balance, amount);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("복구 후 잔액이 허용 범위를 초과합니다.");
        }
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public long getBalance() { return balance; }
    public long getDepositBalance() { return depositBalance; }
}
