package com.greenlink.shopcore.domain.auth;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShopAccessPolicyTest {

    private static final ShopBrand BRAND = ShopBrand.of(100L, 7L, "브랜드");

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
}
