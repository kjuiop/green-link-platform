package com.greenlink.core.application.auth;

import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAuthoritiesTest {

    @Test
    void 슈퍼관리자는_회사유형과_무관하게_역할만_가진다() {
        assertThat(AdminAuthorities.from(AdminRole.SUPER_ADMIN, null)).containsExactly("SUPER_ADMIN");
        assertThat(AdminAuthorities.from(AdminRole.SUPER_ADMIN, CompanyType.VENDOR)).containsExactly("SUPER_ADMIN");
        assertThat(AdminAuthorities.capabilities(AdminRole.SUPER_ADMIN, null)).isEmpty();
    }

    @Test
    void 파트너관리자_발주사는_ORDERING_권한() {
        assertThat(AdminAuthorities.from(AdminRole.PARTNER_ADMIN, CompanyType.ORDERING))
            .containsExactlyInAnyOrder("PARTNER_ADMIN", "ORDERING");
    }

    @Test
    void 파트너관리자_수주사는_VENDOR_권한() {
        assertThat(AdminAuthorities.from(AdminRole.PARTNER_ADMIN, CompanyType.VENDOR))
            .containsExactlyInAnyOrder("PARTNER_ADMIN", "VENDOR");
    }

    @Test
    void 파트너관리자_수발주사는_두_권한_모두_가진다() {
        assertThat(AdminAuthorities.from(AdminRole.PARTNER_ADMIN, CompanyType.BOTH))
            .containsExactlyInAnyOrder("PARTNER_ADMIN", "ORDERING", "VENDOR");
        assertThat(AdminAuthorities.capabilities(AdminRole.PARTNER_ADMIN, CompanyType.BOTH))
            .isEqualTo(EnumSet.of(CompanyType.ORDERING, CompanyType.VENDOR));
    }

    @Test
    void 파트너관리자도_회사유형이_없으면_역할만_가진다() {
        assertThat(AdminAuthorities.from(AdminRole.PARTNER_ADMIN, null)).containsExactly("PARTNER_ADMIN");
    }

    @Test
    void 브랜드관리자는_온우림_회사유형_권한을_갖지_않는다() {
        assertThat(AdminAuthorities.from(AdminRole.BRAND_ADMIN, CompanyType.BOTH)).containsExactly("BRAND_ADMIN");
        assertThat(AdminAuthorities.capabilities(AdminRole.BRAND_ADMIN, CompanyType.BOTH)).isEmpty();
    }

    @Test
    void 역할이_없으면_권한이_없다() {
        assertThat(AdminAuthorities.from(null, CompanyType.BOTH)).isEmpty();
        assertThat(AdminAuthorities.capabilities(null, CompanyType.BOTH)).isEmpty();
    }
}
