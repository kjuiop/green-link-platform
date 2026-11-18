package com.greenlink.core.application.product.service;

import com.greenlink.core.application.product.usecase.SyncBaseProductsUseCase;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.BaseProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BaseProductSyncService implements SyncBaseProductsUseCase {

    private final BaseProductRepository baseProductRepository;
    private final ProductRepository productRepository;

    public BaseProductSyncService(BaseProductRepository baseProductRepository,
                                  ProductRepository productRepository) {
        this.baseProductRepository = baseProductRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void syncBaseProducts(Long companyId) {
        Set<Long> copied = new HashSet<>(productRepository.findBaseProductIdsByCompanyId(companyId));
        List<Product> missing = baseProductRepository.findAll().stream()
                .filter(b -> !copied.contains(b.getId()))
                .map(b -> Product.copyOf(companyId, b))
                .toList();
        if (!missing.isEmpty()) {
            productRepository.saveAll(missing);
        }
    }
}
