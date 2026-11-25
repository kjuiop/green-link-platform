package com.greenlink.shopcore.domain.auth;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShopAccessPolicyTest {

    private static final ShopBrand BRAND = ShopBrand.of(100L, 7L, "브랜드", null);

    @Test
    void 슈퍼관리자는_모든_브랜드에_접근한다() {
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.SUPER_ADMIN, null, Set.of(), BRAND)).isTrue();
    }

    @Test
    void 파트너_관리자는_자기_회사_브랜드면_배정과_무관하게_접근한다() {
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.PARTNER_ADMIN, 7L, Set.of(), BRAND)).isTrue();
    }

    @Test
    void 파트너_관리자는_다른_회사_브랜드에_접근할_수_없다() {
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.PARTNER_ADMIN, 8L, Set.of(100L), BRAND)).isFalse();
    }

    @Test
    void 회사가_없는_파트너_관리자는_접근할_수_없다() {
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.PARTNER_ADMIN, null, Set.of(), BRAND)).isFalse();
    }

    @Test
    void 브랜드_관리자는_배정된_자기_회사_브랜드만_접근한다() {
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.BRAND_ADMIN, 7L, Set.of(100L, 101L), BRAND)).isTrue();
    }

    @Test
    void 브랜드_관리자는_배정되지_않은_브랜드에_접근할_수_없다() {
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.BRAND_ADMIN, 7L, Set.of(101L), BRAND)).isFalse();
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.BRAND_ADMIN, 7L, Set.of(), BRAND)).isFalse();
    }

    @Test
    void 브랜드_관리자는_배정되어_있어도_다른_회사_브랜드에는_접근할_수_없다() {
        assertThat(ShopAccessPolicy.canAccessBrand(ShopRole.BRAND_ADMIN, 8L, Set.of(100L), BRAND)).isFalse();
    }

    @Test
    void 역할이_없으면_접근할_수_없다() {
        assertThat(ShopAccessPolicy.canAccessBrand(null, 7L, Set.of(100L), BRAND)).isFalse();
    }

    @Test
    void 회사_관리_권한은_슈퍼관리자는_전체_파트너는_자기_회사만_브랜드_관리자는_불가하다() {
        assertThat(ShopAccessPolicy.canManageCompany(ShopRole.SUPER_ADMIN, null, 7L)).isTrue();
        assertThat(ShopAccessPolicy.canManageCompany(ShopRole.PARTNER_ADMIN, 7L, 7L)).isTrue();
        assertThat(ShopAccessPolicy.canManageCompany(ShopRole.PARTNER_ADMIN, 8L, 7L)).isFalse();
        assertThat(ShopAccessPolicy.canManageCompany(ShopRole.PARTNER_ADMIN, null, 7L)).isFalse();
        assertThat(ShopAccessPolicy.canManageCompany(ShopRole.BRAND_ADMIN, 7L, 7L)).isFalse();
        assertThat(ShopAccessPolicy.canManageCompany(null, 7L, 7L)).isFalse();
    }

    @Test
    void 역할_부여는_슈퍼관리자는_모두_파트너는_브랜드_관리자만_가능하다() {
        assertThat(ShopAccessPolicy.canGrantRole(ShopRole.SUPER_ADMIN, ShopRole.PARTNER_ADMIN)).isTrue();
        assertThat(ShopAccessPolicy.canGrantRole(ShopRole.SUPER_ADMIN, ShopRole.BRAND_ADMIN)).isTrue();
        assertThat(ShopAccessPolicy.canGrantRole(ShopRole.PARTNER_ADMIN, ShopRole.BRAND_ADMIN)).isTrue();
        assertThat(ShopAccessPolicy.canGrantRole(ShopRole.PARTNER_ADMIN, ShopRole.PARTNER_ADMIN)).isFalse();
        assertThat(ShopAccessPolicy.canGrantRole(ShopRole.BRAND_ADMIN, ShopRole.BRAND_ADMIN)).isFalse();
        assertThat(ShopAccessPolicy.canGrantRole(null, ShopRole.BRAND_ADMIN)).isFalse();
        assertThat(ShopAccessPolicy.canGrantRole(ShopRole.SUPER_ADMIN, null)).isFalse();
    }

    @Test
    void 역할_변경은_슈퍼관리자만_가능하다() {
        assertThat(ShopAccessPolicy.canChangeRole(ShopRole.SUPER_ADMIN)).isTrue();
        assertThat(ShopAccessPolicy.canChangeRole(ShopRole.PARTNER_ADMIN)).isFalse();
        assertThat(ShopAccessPolicy.canChangeRole(ShopRole.BRAND_ADMIN)).isFalse();
        assertThat(ShopAccessPolicy.canChangeRole(null)).isFalse();
    }
}
