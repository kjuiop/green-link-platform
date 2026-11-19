package com.greenlink.core.application.order.service;

import com.greenlink.core.application.order.dto.OrderItemCommand;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.order.model.OrderItem;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 수주사 상품 단가와 회사 수수료로 주문 항목 금액을 서버에서 확정한다. 클라이언트가 보낸 단가는 사용하지 않는다. */
@Component
public class OrderItemPricer {

    public record Priced(List<OrderItem> items, long vendorFee) {}

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    public OrderItemPricer(ProductRepository productRepository, CompanyRepository companyRepository) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
    }

    public Priced price(Long vendorCompanyId, List<OrderItemCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("주문 상품은 1개 이상이어야 합니다.");
        }
        if (vendorCompanyId == null) {
            throw new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND);
        }
        Company vendor = companyRepository.findById(vendorCompanyId)
                .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
        long fee = vendor.getVendorFee();

        List<OrderItem> items = new ArrayList<>();
        long totalQuantity = 0;
        for (OrderItemCommand c : commands) {
            if (c.productId() == null) {
                throw new BusinessException(ProductErrorCode.PRODUCT_REQUIRED);
            }
            Product product = productRepository.findByIdAndCompanyId(c.productId(), vendorCompanyId)
                    .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
            if (!product.isEnabled()) {
                throw new BusinessException(ProductErrorCode.PRODUCT_DISABLED);
            }
            long unitPrice;
            try {
                unitPrice = Math.addExact(product.getPrice(), fee);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("금액이 허용 범위를 초과했습니다.");
            }
            items.add(OrderItem.create(product.getId(), product.getName(), unitPrice, c.quantity()));
            totalQuantity += c.quantity();
        }
        try {
            return new Priced(items, Math.multiplyExact(fee, totalQuantity));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("금액이 허용 범위를 초과했습니다.");
        }
    }
}
