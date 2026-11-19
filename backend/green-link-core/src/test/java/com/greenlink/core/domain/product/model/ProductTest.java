package com.greenlink.core.domain.product.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private BaseProduct base(long price) {
        return BaseProduct.of(1L, "근조화환 3단", price, "메모", null, 1, ProductCategory.MEMORIAL);
    }

    @Test
    void 직접_등록_상품은_회사가_필수다() {
        assertThatThrownBy(() -> Product.createOwn(null, "상품", 1000, null, 0, ProductCategory.MEMORIAL))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 복사본은_기본_가격으로_시작하고_ON이다() {
        Product copy = Product.copyOf(10L, base(50000));

        assertThat(copy.getCompanyId()).isEqualTo(10L);
        assertThat(copy.getBaseProductId()).isEqualTo(1L);
        assertThat(copy.getPrice()).isEqualTo(50000);
        assertThat(copy.isEnabled()).isTrue();
        assertThat(copy.isBaseCopy()).isTrue();
    }

    @Test
    void 복사본은_이름_수정이_거부된다() {
        Product copy = Product.copyOf(10L, base(50000));

        assertThatThrownBy(() -> copy.update("다른이름", 1000, null, 0, ProductCategory.MEMORIAL))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_BASE_COPY_READONLY));
    }

    @Test
    void 복사본도_가격과_ON_OFF는_바꿀_수_있다() {
        Product copy = Product.copyOf(10L, base(50000));

        copy.changeSetting(45000, false);

        assertThat(copy.getPrice()).isEqualTo(45000);
        assertThat(copy.isEnabled()).isFalse();
    }

    @Test
    void 음수_가격은_거부된다() {
        Product copy = Product.copyOf(10L, base(50000));

        assertThatThrownBy(() -> copy.changeSetting(-1, true)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 기본_상품_수정은_이름_메모_정렬만_반영하고_가격은_유지한다() {
        Product copy = Product.copyOf(10L, base(50000));
        copy.changeSetting(45000, true);

        copy.applyBaseUpdate(BaseProduct.of(1L, "새이름", 99000, "새메모", null, 7, ProductCategory.MEMORIAL));

        assertThat(copy.getName()).isEqualTo("새이름");
        assertThat(copy.getMemo()).isEqualTo("새메모");
        assertThat(copy.getSortOrder()).isEqualTo(7);
        assertThat(copy.getPrice()).isEqualTo(45000);
    }

    @Test
    void 복사본은_기본_상품의_종류를_따른다() {
        Product copy = Product.copyOf(10L, base(50000));

        assertThat(copy.getCategory()).isEqualTo(ProductCategory.MEMORIAL);
    }

    @Test
    void 직접_등록_상품은_종류가_필수다() {
        assertThatThrownBy(() -> Product.createOwn(10L, "상품", 1000, null, 0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 종류는 필수입니다.");
    }

    @Test
    void 직접_등록_상품은_종류를_수정할_수_있다() {
        Product own = Product.createOwn(10L, "상품", 1000, null, 0, ProductCategory.FLOWER);

        own.update("상품", 1000, null, 0, ProductCategory.INDOOR);

        assertThat(own.getCategory()).isEqualTo(ProductCategory.INDOOR);
    }

    @Test
    void 기본_상품_종류가_바뀌면_복사본에도_반영된다() {
        Product copy = Product.copyOf(10L, base(50000));

        copy.applyBaseUpdate(BaseProduct.of(1L, "근조화환 3단", 50000, "메모", null, 1, ProductCategory.ANNIVERSARY));

        assertThat(copy.getCategory()).isEqualTo(ProductCategory.ANNIVERSARY);
    }
}
