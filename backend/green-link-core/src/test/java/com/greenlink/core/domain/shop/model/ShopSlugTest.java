package com.greenlink.core.domain.shop.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopSlugTest {

    @Test
    void 대문자와_공백은_소문자로_정규화한다() {
        assertThat(ShopSlug.normalize("  Flower-Shop1 ")).isEqualTo("flower-shop1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "-abc", "abc-", "a--b", "한글가게", "has space", "under_score", "admin", "api", "s"})
    void 형식에_맞지_않거나_예약어면_거부한다(String raw) {
        assertThatThrownBy(() -> ShopSlug.normalize(raw))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.INVALID_SLUG);
    }

    @Test
    void 너무_긴_slug는_거부한다() {
        assertThat(ShopSlug.normalizeOrNull("a".repeat(31))).isNull();
        assertThat(ShopSlug.normalizeOrNull("a".repeat(30))).isNotNull();
    }

    @Test
    void 공개조회용_normalizeOrNull은_예외_대신_null() {
        assertThat(ShopSlug.normalizeOrNull(null)).isNull();
        assertThat(ShopSlug.normalizeOrNull("../etc")).isNull();
    }
}
