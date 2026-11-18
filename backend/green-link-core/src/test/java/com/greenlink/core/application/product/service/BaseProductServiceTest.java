package com.greenlink.core.application.product.service;

import com.greenlink.core.application.product.dto.ProductCommand;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.product.model.BaseProduct;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.BaseProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BaseProductServiceTest {

    @Mock BaseProductRepository baseProductRepository;
    @Mock ProductRepository productRepository;
    @Mock CompanyRepository companyRepository;
    @InjectMocks BaseProductService service;

    private Company company(Long id, CompanyType type) {
        return Company.of(id, null, "업체" + id, "대표", "02-0000-0000", null, "서울", "", type, CompanyStatus.ACTIVE, true,
                null, null, null, null, null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 기본_상품_생성_시_수주_가능_회사마다_복사본이_생긴다() {
        given(baseProductRepository.save(any(BaseProduct.class)))
                .willReturn(BaseProduct.of(5L, "근조화환", 50000, null, null, 1));
        given(companyRepository.findAllByTypeIn(List.of(CompanyType.VENDOR, CompanyType.BOTH)))
                .willReturn(List.of(company(1L, CompanyType.VENDOR), company(2L, CompanyType.BOTH)));

        service.createBaseProduct(new ProductCommand("근조화환", 50000, null, 1));

        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.forClass(List.class);
        verify(productRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Product::getCompanyId).containsExactly(1L, 2L);
        assertThat(captor.getValue()).allSatisfy(p -> {
            assertThat(p.getBaseProductId()).isEqualTo(5L);
            assertThat(p.getPrice()).isEqualTo(50000);
            assertThat(p.isEnabled()).isTrue();
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void 기본_상품_수정은_복사본에_가격을_전파하지_않는다() {
        BaseProduct base = BaseProduct.of(5L, "근조화환", 50000, null, null, 1);
        Product copy = Product.of(100L, 1L, 5L, "근조화환", 50000, null, null, 1, true);
        given(baseProductRepository.findById(5L)).willReturn(Optional.of(base));
        given(baseProductRepository.save(any(BaseProduct.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(productRepository.findAllByBaseProductId(5L)).willReturn(List.of(copy));

        service.updateBaseProduct(5L, new ProductCommand("근조화환 특대", 99000, "메모", 3));

        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.forClass(List.class);
        verify(productRepository).saveAll(captor.capture());
        Product saved = captor.getValue().get(0);
        assertThat(saved.getName()).isEqualTo("근조화환 특대");
        assertThat(saved.getSortOrder()).isEqualTo(3);
        assertThat(saved.getPrice()).isEqualTo(50000);
    }

    @Test
    void 기본_상품_삭제_시_복사본을_먼저_삭제한다() {
        given(baseProductRepository.findById(5L))
                .willReturn(Optional.of(BaseProduct.of(5L, "근조화환", 50000, null, null, 1)));

        service.deleteBaseProduct(5L);

        verify(productRepository).deleteAllByBaseProductId(5L);
        verify(baseProductRepository).delete(5L);
    }

    @Test
    void 없는_기본_상품_삭제는_404다() {
        given(baseProductRepository.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteBaseProduct(9L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.BASE_PRODUCT_NOT_FOUND));
        verify(productRepository, never()).deleteAllByBaseProductId(any());
    }
}
