package com.greenlink.core.application.order.service;

import com.greenlink.core.application.order.dto.OrderItemCommand;
import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.order.model.OrderItem;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.model.ProductCategory;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderItemPricerTest {

    private static final Long COMPANY_ID = 10L;

    @Mock ProductRepository productRepository;
    @Mock CompanyRepository companyRepository;
    @InjectMocks OrderItemPricer pricer;

    private void givenCompany(long fee) {
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(
                Company.of(COMPANY_ID, "000-00-00010", "수주사", null, "02-000-0010", null, null, null,
                        CompanyType.VENDOR, CompanyStatus.ACTIVE, true, null, null, null, null, null,
                        BusinessStatus.OPEN, fee)));
    }

    private Product product(Long id, long price, boolean enabled) {
        return Product.of(id, COMPANY_ID, null, "상품" + id, price, null, null, 0, enabled, ProductCategory.MEMORIAL);
    }

    @Test
    void 단가에_수수료를_더하고_수수료는_전체_수량만큼_스냅샷한다() {
        givenCompany(3000L);
        given(productRepository.findByIdAndCompanyId(1L, COMPANY_ID)).willReturn(Optional.of(product(1L, 50000L, true)));
        given(productRepository.findByIdAndCompanyId(2L, COMPANY_ID)).willReturn(Optional.of(product(2L, 70000L, true)));

        OrderItemPricer.Priced priced = pricer.price(COMPANY_ID, List.of(
                new OrderItemCommand(1L, "무시됨", 1L, 1),
                new OrderItemCommand(2L, "무시됨", 1L, 2)));

        assertThat(priced.items()).extracting(OrderItem::getUnitPrice).containsExactly(53000L, 73000L);
        assertThat(priced.items()).extracting(OrderItem::getAmount).containsExactly(53000L, 146000L);
        assertThat(priced.items()).extracting(OrderItem::getProductName).containsExactly("상품1", "상품2");
        assertThat(priced.vendorFee()).isEqualTo(9000L);
    }

    @Test
    void 수수료가_0원이면_원가_그대로다() {
        givenCompany(0L);
        given(productRepository.findByIdAndCompanyId(1L, COMPANY_ID)).willReturn(Optional.of(product(1L, 50000L, true)));

        OrderItemPricer.Priced priced = pricer.price(COMPANY_ID, List.of(new OrderItemCommand(1L, "x", 0L, 1)));

        assertThat(priced.items().get(0).getUnitPrice()).isEqualTo(50000L);
        assertThat(priced.vendorFee()).isZero();
    }

    @Test
    void 다른_회사_상품은_거부한다() {
        givenCompany(3000L);
        given(productRepository.findByIdAndCompanyId(1L, COMPANY_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pricer.price(COMPANY_ID, List.of(new OrderItemCommand(1L, "x", 0L, 1))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Test
    void 비활성_상품은_거부한다() {
        givenCompany(3000L);
        given(productRepository.findByIdAndCompanyId(1L, COMPANY_ID)).willReturn(Optional.of(product(1L, 50000L, false)));

        assertThatThrownBy(() -> pricer.price(COMPANY_ID, List.of(new OrderItemCommand(1L, "x", 0L, 1))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_DISABLED));
    }

    @Test
    void 상품_미선택은_거부한다() {
        givenCompany(3000L);

        assertThatThrownBy(() -> pricer.price(COMPANY_ID, List.of(new OrderItemCommand(null, "x", 50000L, 1))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_REQUIRED));
    }

    @Test
    void 수주사가_없으면_거부한다() {
        assertThatThrownBy(() -> pricer.price(null, List.of(new OrderItemCommand(1L, "x", 0L, 1))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(VendorErrorCode.VENDOR_NOT_FOUND));
    }

    @Test
    void 항목이_없으면_거부한다() {
        assertThatThrownBy(() -> pricer.price(COMPANY_ID, List.of())).isInstanceOf(IllegalArgumentException.class);
    }
}
