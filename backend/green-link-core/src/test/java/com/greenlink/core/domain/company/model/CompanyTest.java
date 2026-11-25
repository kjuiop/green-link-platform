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
    void 쇼핑몰_가입_회사는_온우림이_꺼지고_사이트만_켜진다() {
        Company c = Company.createForShop("123-45-67890", "꽃집", "홍길동", "02-111-2222");

        assertThat(c.isOnurimEnabled()).isFalse();
        assertThat(c.isSiteEnabled()).isTrue();
        assertThat(c.getType()).isNull();
        assertThat(c.getStatus()).isEqualTo(CompanyStatus.ACTIVE);
        assertThat(c.getBusinessNo()).isEqualTo("123-45-67890");
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

    private static Company ofType(CompanyType type) {
        return Company.of(1L, "000-00-00001", "회사", null, null, null, null, null,
                type, CompanyStatus.ACTIVE, true, null, null, null, null, null, BusinessStatus.OPEN);
    }

    @Test
    void 기본값은_온우림사용_켜짐_사이트사용_꺼짐() {
        Company company = ofType(CompanyType.VENDOR);
        assertThat(company.isOnurimEnabled()).isTrue();
        assertThat(company.isSiteEnabled()).isFalse();
        assertThat(company.isOnurimCompany()).isTrue();
    }

    @Test
    void 온우림사용이_꺼지면_온우림_회사가_아니다() {
        Company company = ofType(CompanyType.VENDOR);
        company.restoreServiceFlags(false, true);
        assertThat(company.isOnurimCompany()).isFalse();
        assertThat(company.isSiteEnabled()).isTrue();
    }

    @Test
    void 타입이_없으면_온우림_회사가_아니다() {
        assertThat(ofType(null).isOnurimCompany()).isFalse();
    }

    @Test
    void expandType_타입이_없으면_요청_타입으로_설정() {
        Company company = ofType(null);
        company.expandType(CompanyType.ORDERING);
        assertThat(company.getType()).isEqualTo(CompanyType.ORDERING);
    }

    @Test
    void expandType_타입이_있으면_BOTH로_확장() {
        Company company = ofType(CompanyType.VENDOR);
        company.expandType(CompanyType.ORDERING);
        assertThat(company.getType()).isEqualTo(CompanyType.BOTH);
    }
}
