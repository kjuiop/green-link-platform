package com.greenlink.core.domain.company.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyTest {

    private Company company() {
        return Company.of(1L, "000-00-00001", "수주사", null, "02-000-0001", null, null, null,
                CompanyType.VENDOR, CompanyStatus.ACTIVE, true, null, null, null, null, null,
                BusinessStatus.OPEN);
    }

    @Test
    void 수수료_기본값은_3000원이다() {
        assertThat(company().getVendorFee()).isEqualTo(3000L);
    }

    @Test
    void 수수료를_변경한다() {
        Company c = company();
        c.changeVendorFee(0L);
        assertThat(c.getVendorFee()).isZero();
        c.changeVendorFee(Company.MAX_VENDOR_FEE);
        assertThat(c.getVendorFee()).isEqualTo(Company.MAX_VENDOR_FEE);
    }

    @Test
    void 음수_수수료는_거부한다() {
        assertThatThrownBy(() -> company().changeVendorFee(-1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상한_초과_수수료는_거부한다() {
        assertThatThrownBy(() -> company().changeVendorFee(Company.MAX_VENDOR_FEE + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
