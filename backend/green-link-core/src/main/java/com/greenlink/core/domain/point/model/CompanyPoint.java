package com.greenlink.core.domain.point.model;

public class CompanyPoint {

    private Long id;
    private Long companyId;
    private long balance;

    private CompanyPoint() {}

    public static CompanyPoint init(Long companyId) {
        CompanyPoint p = new CompanyPoint();
        p.companyId = companyId;
        p.balance = 0L;
        return p;
    }

    public static CompanyPoint of(Long id, Long companyId, long balance) {
        CompanyPoint p = new CompanyPoint();
        p.id = id;
        p.companyId = companyId;
        p.balance = balance;
        return p;
    }

    public void charge(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        this.balance += amount;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public long getBalance() { return balance; }
}
