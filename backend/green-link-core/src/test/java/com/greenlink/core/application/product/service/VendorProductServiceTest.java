package com.greenlink.core.application.product.service;

import com.greenlink.core.application.product.dto.ProductCommand;
import com.greenlink.core.application.product.dto.VendorProductResult;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.product.model.BaseProduct;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.BaseProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VendorProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock BaseProductRepository baseProductRepository;
    @Mock AdminRepository adminRepository;
    @InjectMocks VendorProductService service;

    private Product own(Long id, Long companyId) {
        return Product.of(id, companyId, null, "자체상품", 30000, null, null, 0, true);
    }

    private Product copy(Long id, Long companyId, Long baseId) {
        return Product.of(id, companyId, baseId, "기본상품", 50000, null, null, 0, true);
    }

    @Test
    void 자체_상품을_등록하면_해당_회사_소속이다() {
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

        VendorProductResult result = service.createProduct(10L, new ProductCommand("자체상품", 30000, null, 0));

        assertThat(result.baseProductId()).isNull();
        assertThat(result.enabled()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void 다른_수주사_상품_수정은_404다() {
        given(productRepository.findByIdAndCompanyId(1L, 10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProduct(10L, 1L, new ProductCommand("x", 1, null, 0)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Test
    void 기본_상품_복사본은_수정_삭제가_거부된다() {
        given(productRepository.findByIdAndCompanyId(1L, 10L)).willReturn(Optional.of(copy(1L, 10L, 5L)));

        assertThatThrownBy(() -> service.updateProduct(10L, 1L, new ProductCommand("x", 1, null, 0)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_BASE_COPY_READONLY));
        assertThatThrownBy(() -> service.deleteProduct(10L, 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_BASE_COPY_READONLY));
        verify(productRepository, never()).delete(any());
    }

    @Test
    void 자체_상품은_삭제할_수_있다() {
        given(productRepository.findByIdAndCompanyId(2L, 10L)).willReturn(Optional.of(own(2L, 10L)));

        service.deleteProduct(10L, 2L);

        verify(productRepository).delete(2L);
    }

    @Test
    void 기본_상품_복사본의_가격과_ON_OFF를_바꾸면_기본_가격도_함께_내려준다() {
        given(productRepository.findByIdAndCompanyId(1L, 10L)).willReturn(Optional.of(copy(1L, 10L, 5L)));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));
        given(baseProductRepository.findById(5L))
                .willReturn(Optional.of(BaseProduct.of(5L, "기본상품", 50000, null, null, 0)));

        VendorProductResult result = service.changeSetting(10L, 1L, 42000, false);

        assertThat(result.price()).isEqualTo(42000);
        assertThat(result.enabled()).isFalse();
        assertThat(result.basePrice()).isEqualTo(50000);
    }

    @Test
    void 음수_가격_설정은_거부된다() {
        given(productRepository.findByIdAndCompanyId(1L, 10L)).willReturn(Optional.of(own(1L, 10L)));

        assertThatThrownBy(() -> service.changeSetting(10L, 1L, -1, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 관리_목록은_OFF_상품도_포함하고_기본_가격을_채운다() {
        PageRequest pageable = PageRequest.of(0, 20);
        given(productRepository.search(10L, null, false, pageable))
                .willReturn(new PageImpl<>(List.of(copy(1L, 10L, 5L), own(2L, 10L)), pageable, 2));
        given(baseProductRepository.findAllByIdIn(any()))
                .willReturn(List.of(BaseProduct.of(5L, "기본상품", 55000, null, null, 0)));

        var page = service.getProducts(10L, null, pageable);

        assertThat(page.content()).extracting(VendorProductResult::basePrice).containsExactly(55000L, null);
    }

    @Test
    void 발주용_검색은_수주사_계정의_회사_기준_ON_상품만_조회한다() {
        PageRequest pageable = PageRequest.of(0, 20);
        given(adminRepository.findById(7L)).willReturn(Optional.of(
                Admin.of(7L, "v", "pw", "홍", "010", null, 10L, AdminStatus.ACTIVE, "2026-10-22T10:00:00", null)));
        given(productRepository.search(10L, "화환", true, pageable))
                .willReturn(new PageImpl<>(List.of(own(2L, 10L)), pageable, 1));

        var page = service.searchAvailable(7L, "화환", pageable);

        assertThat(page.content()).hasSize(1);
        verify(productRepository).search(10L, "화환", true, pageable);
    }

    @Test
    void 존재하지_않는_수주사로_검색하면_404다() {
        given(adminRepository.findById(7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.searchAvailable(7L, null, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);
    }
}
