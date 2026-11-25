package com.greenlink.shopcore.domain.brand;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopBrandTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 25, 10, 0);

    private void assertInvalidName(String name) {
        assertThatThrownBy(() -> ShopBrand.create(7L, name)).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.BRAND_NAME_INVALID));
    }

    @Test
    void 브랜드를_만들면_이름이_trim되고_삭제되지_않은_상태다() {
        ShopBrand brand = ShopBrand.create(7L, "  온우림  ");

        assertThat(brand.getName()).isEqualTo("온우림");
        assertThat(brand.getCompanyId()).isEqualTo(7L);
        assertThat(brand.isDeleted()).isFalse();
    }

    @Test
    void 이름이_비어있거나_공백이거나_null이면_거부된다() {
        assertInvalidName(null);
        assertInvalidName("");
        assertInvalidName("   ");
    }

    @Test
    void 이름은_100자까지만_허용된다() {
        assertThat(ShopBrand.create(7L, "가".repeat(100)).getName()).hasSize(100);
        assertInvalidName("가".repeat(101));
    }

    @Test
    void 이름을_변경하면_검증과_trim이_적용된다() {
        ShopBrand brand = ShopBrand.create(7L, "기존");

        brand.rename("  새이름 ");

        assertThat(brand.getName()).isEqualTo("새이름");
        assertThatThrownBy(() -> brand.rename(" ")).isInstanceOf(ShopBusinessException.class);
        assertThat(brand.getName()).isEqualTo("새이름");
    }

    @Test
    void 삭제는_소프트_삭제이고_삭제_시각은_최초_한번만_기록된다() {
        ShopBrand brand = ShopBrand.of(1L, 7L, "브랜드", null);

        brand.delete(T0);
        brand.delete(T0.plusDays(1));

        assertThat(brand.isDeleted()).isTrue();
        assertThat(brand.getDeletedAt()).isEqualTo(T0);
        assertThat(brand.getName()).isEqualTo("브랜드");
    }
}
