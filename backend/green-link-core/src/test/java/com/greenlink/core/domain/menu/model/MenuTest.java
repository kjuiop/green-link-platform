package com.greenlink.core.domain.menu.model;

import com.greenlink.core.application.auth.AdminAuthorities;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MenuTest {

    private static final List<AdminRole> SUPER_AND_PARTNER = List.of(AdminRole.SUPER_ADMIN, AdminRole.PARTNER_ADMIN);

    private static Menu menu(List<AdminRole> roles, List<CompanyType> types) {
        return Menu.create("메뉴", "/m", 1, null, roles, types);
    }

    private static Set<CompanyType> caps(CompanyType type) {
        return AdminAuthorities.capabilities(AdminRole.PARTNER_ADMIN, type);
    }

    @Test
    void 관리자_권한이_메뉴_권한에_없으면_숨긴다() {
        Menu m = menu(List.of(AdminRole.SUPER_ADMIN), List.of());
        assertThat(m.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.ORDERING))).isFalse();
        assertThat(m.isAccessibleBy(AdminRole.BRAND_ADMIN, EnumSet.noneOf(CompanyType.class))).isFalse();
        assertThat(m.isAccessibleBy(AdminRole.SUPER_ADMIN, EnumSet.noneOf(CompanyType.class))).isTrue();
    }

    @Test
    void 회사유형이_비어있으면_모든_회사유형에_노출한다() {
        Menu m = menu(SUPER_AND_PARTNER, List.of());
        assertThat(m.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.ORDERING))).isTrue();
        assertThat(m.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.VENDOR))).isTrue();
        assertThat(m.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.BOTH))).isTrue();
    }

    @Test
    void 권한과_회사유형은_AND_조건이다() {
        Menu ordering = menu(SUPER_AND_PARTNER, List.of(CompanyType.ORDERING));
        assertThat(ordering.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.ORDERING))).isTrue();
        assertThat(ordering.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.VENDOR))).isFalse();

        Menu vendor = menu(SUPER_AND_PARTNER, List.of(CompanyType.VENDOR));
        assertThat(vendor.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.VENDOR))).isTrue();
        assertThat(vendor.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.ORDERING))).isFalse();
    }

    @Test
    void 수발주사는_발주_메뉴와_수주_메뉴를_모두_본다() {
        Menu ordering = menu(SUPER_AND_PARTNER, List.of(CompanyType.ORDERING));
        Menu vendor = menu(SUPER_AND_PARTNER, List.of(CompanyType.VENDOR));
        assertThat(ordering.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.BOTH))).isTrue();
        assertThat(vendor.isAccessibleBy(AdminRole.PARTNER_ADMIN, caps(CompanyType.BOTH))).isTrue();
    }

    @Test
    void 슈퍼관리자는_회사유형_제한을_받지_않는다() {
        Menu m = menu(SUPER_AND_PARTNER, List.of(CompanyType.VENDOR));
        assertThat(m.isAccessibleBy(AdminRole.SUPER_ADMIN, EnumSet.noneOf(CompanyType.class))).isTrue();
    }

    @Test
    void 권한이_없으면_숨긴다() {
        assertThat(menu(SUPER_AND_PARTNER, List.of()).isAccessibleBy(null, EnumSet.noneOf(CompanyType.class))).isFalse();
    }
}
