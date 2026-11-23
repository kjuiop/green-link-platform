package com.greenlink.core.application.settlement.dto;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.settlement.port.SettlementQueryPort.OrderSettlementRaw;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementOrderResultTest {

    private final OrderSettlementRaw raw = new OrderSettlementRaw(
            1L, "DF-1", 2L, "상품", "장소", "주문자", "2026-11-01T10:00:00",
            50000L, 40000L, 0L, 3000L, 0L);

    @Test
    void forOrdering_hidesVendorAmountAndFee() {
        Company ordering = Company.create("111-11-11111", "발주사", null, null, null, null, null, CompanyType.ORDERING);

        SettlementOrderResult result = SettlementOrderResult.forOrdering(raw, "수주사", ordering);

        assertThat(result.vendorAmount()).isNull();
        assertThat(result.vendorFee()).isNull();
    }

    @Test
    void forVendor_keepsVendorAmount() {
        SettlementOrderResult result = SettlementOrderResult.forVendor(raw, "발주사");

        assertThat(result.vendorAmount()).isEqualTo(40000L);
    }
}
