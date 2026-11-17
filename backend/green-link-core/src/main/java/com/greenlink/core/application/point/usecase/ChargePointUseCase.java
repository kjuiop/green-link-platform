package com.greenlink.core.application.point.usecase;

public interface ChargePointUseCase {
    void charge(Long companyId, long amount, String memo, Long adminId);
}
