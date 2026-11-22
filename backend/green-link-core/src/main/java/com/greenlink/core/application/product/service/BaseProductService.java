package com.greenlink.core.application.product.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.BaseProductResult;
import com.greenlink.core.application.product.dto.ProductCommand;
import com.greenlink.core.application.product.usecase.ManageBaseProductsUseCase;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.product.model.BaseProduct;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.BaseProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BaseProductService implements ManageBaseProductsUseCase {

    private final BaseProductRepository baseProductRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    public BaseProductService(BaseProductRepository baseProductRepository,
                              ProductRepository productRepository,
                              CompanyRepository companyRepository) {
        this.baseProductRepository = baseProductRepository;
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public PageResult<BaseProductResult> getBaseProducts(Pageable pageable) {
        return PageResult.of(baseProductRepository.findAll(pageable).map(this::toResult));
    }

    @Override
    public List<BaseProductResult> getAllBaseProducts() {
        return baseProductRepository.findAll().stream().map(this::toResult).toList();
    }

    @Override
    @Transactional
    public BaseProductResult createBaseProduct(ProductCommand cmd) {
        BaseProduct saved = baseProductRepository.save(
                BaseProduct.create(cmd.name(), cmd.price(), cmd.memo(), cmd.sortOrder(), cmd.category()));
        List<Product> copies = companyRepository.findAllByTypeIn(List.of(CompanyType.VENDOR, CompanyType.BOTH))
                .stream()
                .map(c -> Product.copyOf(c.getId(), saved))
                .toList();
        productRepository.saveAll(copies);
        return toResult(saved);
    }

    @Override
    @Transactional
    public BaseProductResult updateBaseProduct(Long id, ProductCommand cmd) {
        BaseProduct base = baseProductRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.BASE_PRODUCT_NOT_FOUND));
        base.update(cmd.name(), cmd.price(), cmd.memo(), cmd.sortOrder(), cmd.category());
        BaseProduct saved = baseProductRepository.save(base);
        List<Product> copies = productRepository.findAllByBaseProductId(id);
        copies.forEach(c -> c.applyBaseUpdate(saved));
        productRepository.saveAll(copies);
        return toResult(saved);
    }

    @Override
    @Transactional
    public void deleteBaseProduct(Long id) {
        if (baseProductRepository.findById(id).isEmpty()) {
            throw new BusinessException(ProductErrorCode.BASE_PRODUCT_NOT_FOUND);
        }
        productRepository.deleteAllByBaseProductId(id);
        baseProductRepository.delete(id);
    }

    private BaseProductResult toResult(BaseProduct p) {
        return new BaseProductResult(p.getId(), p.getName(), p.getPrice(), p.getMemo(), p.getCreatedAt(), p.getSortOrder(),
                p.getCategory() != null ? p.getCategory().name() : null);
    }
}
