package com.greenlink.core.domain.point.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyPointTest {

    @Test
    void hold_movesAmountFromBalanceToDeposit() {
        CompanyPoint p = CompanyPoint.of(1L, 1L, 100_000L, 0L);

        p.hold(30_000L);

        assertThat(p.getBalance()).isEqualTo(70_000L);
        assertThat(p.getDepositBalance()).isEqualTo(30_000L);
    }

    @Test
    void hold_whenBalanceInsufficient_throws() {
        CompanyPoint p = CompanyPoint.of(1L, 1L, 1_000L, 0L);

        assertThatThrownBy(() -> p.hold(1_001L)).isInstanceOf(IllegalStateException.class);
        assertThat(p.getBalance()).isEqualTo(1_000L);
        assertThat(p.getDepositBalance()).isZero();
    }

    @Test
    void release_movesAmountFromDepositBackToBalance() {
        CompanyPoint p = CompanyPoint.of(1L, 1L, 70_000L, 30_000L);

        p.release(10_000L);

        assertThat(p.getBalance()).isEqualTo(80_000L);
        assertThat(p.getDepositBalance()).isEqualTo(20_000L);
    }

    @Test
    void release_whenDepositInsufficient_throws() {
        CompanyPoint p = CompanyPoint.of(1L, 1L, 0L, 5_000L);

        assertThatThrownBy(() -> p.release(5_001L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void settle_consumesDepositWithoutChangingBalance() {
        CompanyPoint p = CompanyPoint.of(1L, 1L, 70_000L, 30_000L);

        p.settle(30_000L);

        assertThat(p.getBalance()).isEqualTo(70_000L);
        assertThat(p.getDepositBalance()).isZero();
    }

    @Test
    void negativeAmounts_areRejected() {
        CompanyPoint p = CompanyPoint.of(1L, 1L, 10L, 10L);

        assertThatThrownBy(() -> p.hold(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> p.release(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> p.settle(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
