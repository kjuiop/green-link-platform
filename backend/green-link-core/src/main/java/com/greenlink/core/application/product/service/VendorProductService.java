package com.greenlink.core.application.product.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.ProductCommand;
import com.greenlink.core.application.product.dto.VendorProductResult;
import com.greenlink.core.application.product.usecase.ManageVendorProductsUseCase;
import com.greenlink.core.application.product.usecase.SearchAvailableProductsUseCase;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.product.model.BaseProduct;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.BaseProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VendorProductService implements ManageVendorProductsUseCase, SearchAvailableProductsUseCase {

    private final ProductRepository productRepository;
    private final BaseProductRepository baseProductRepository;
    private final AdminRepository adminRepository;

    public VendorProductService(ProductRepository productRepository,
                                BaseProductRepository baseProductRepository,
                                AdminRepository adminRepository) {
        this.productRepository = productRepository;
        this.baseProductRepository = baseProductRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public PageResult<VendorProductResult> getProducts(Long companyId, String keyword, Pageable pageable) {
        return toPage(productRepository.search(companyId, keyword, false, pageable));
    }

    @Override
    public PageResult<VendorProductResult> searchAvailable(Long vendorAdminId, String keyword, Pageable pageable) {
        Long companyId = adminRepository.findById(vendorAdminId)
                .map(a -> a.getCompanyId())
                .filter(id -> id != null)
                .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
        return toPage(productRepository.search(companyId, keyword, true, pageable));
    }

    @Override
    @Transactional
    public VendorProductResult createProduct(Long companyId, ProductCommand cmd) {
        Product saved = productRepository.save(
                Product.createOwn(companyId, cmd.name(), cmd.price(), cmd.memo(), cmd.sortOrder()));
        return toResult(saved, null);
    }

    @Override
    @Transactional
    public VendorProductResult updateProduct(Long companyId, Long productId, ProductCommand cmd) {
        Product product = find(companyId, productId);
        product.update(cmd.name(), cmd.price(), cmd.memo(), cmd.sortOrder());
        return toResult(productRepository.save(product), null);
    }

    @Override
    @Transactional
    public void deleteProduct(Long companyId, Long productId) {
        Product product = find(companyId, productId);
        product.requireOwn();
        productRepository.delete(product.getId());
    }

    @Override
    @Transactional
    public VendorProductResult changeSetting(Long companyId, Long productId, long price, boolean enabled) {
        Product product = find(companyId, productId);
        product.changeSetting(price, enabled);
        Product saved = productRepository.save(product);
        Long basePrice = saved.isBaseCopy()
                ? baseProductRepository.findById(saved.getBaseProductId()).map(BaseProduct::getPrice).orElse(null)
                : null;
        return toResult(saved, basePrice);
    }

    private Product find(Long companyId, Long productId) {
        return productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    private PageResult<VendorProductResult> toPage(Page<Product> page) {
        Set<Long> baseIds = page.getContent().stream()
                .map(Product::getBaseProductId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Long> basePrices = baseIds.isEmpty() ? Map.of()
                : baseProductRepository.findAllByIdIn(baseIds).stream()
                        .collect(Collectors.toMap(BaseProduct::getId, BaseProduct::getPrice));
        return PageResult.of(page.map(p -> toResult(p, p.getBaseProductId() == null ? null : basePrices.get(p.getBaseProductId()))));
    }

    private VendorProductResult toResult(Product p, Long basePrice) {
        return new VendorProductResult(p.getId(), p.getName(), p.getPrice(), p.getMemo(), p.getSortOrder(),
                p.isEnabled(), p.getBaseProductId(), basePrice, p.getCreatedAt());
    }
}
