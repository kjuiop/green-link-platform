package com.greenlink.core.application.product.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.CreateProductCommand;
import com.greenlink.core.application.product.dto.ProductResult;
import com.greenlink.core.application.product.dto.UpdateProductCommand;
import com.greenlink.core.application.product.usecase.CreateProductUseCase;
import com.greenlink.core.application.product.usecase.DeleteProductUseCase;
import com.greenlink.core.application.product.usecase.GetAllProductsUseCase;
import com.greenlink.core.application.product.usecase.GetProductsUseCase;
import com.greenlink.core.application.product.usecase.UpdateProductUseCase;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.product.ProductErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService implements CreateProductUseCase, UpdateProductUseCase,
        DeleteProductUseCase, GetProductsUseCase, GetAllProductsUseCase {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductResult createProduct(CreateProductCommand cmd) {
        Product product = Product.create(cmd.name(), cmd.price(), cmd.memo());
        return toResult(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResult updateProduct(Long id, UpdateProductCommand cmd) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
        product.update(cmd.name(), cmd.price(), cmd.memo());
        return toResult(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.delete(id);
    }

    @Override
    public PageResult<ProductResult> getProducts(Pageable pageable) {
        return PageResult.of(productRepository.findAll(pageable).map(this::toResult));
    }

    @Override
    public List<ProductResult> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResult).toList();
    }

    private ProductResult toResult(Product p) {
        return new ProductResult(p.getId(), p.getName(), p.getPrice(), p.getMemo(), p.getCreatedAt());
    }
}
