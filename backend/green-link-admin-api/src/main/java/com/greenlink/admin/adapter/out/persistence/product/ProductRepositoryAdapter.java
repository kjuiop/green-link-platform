package com.greenlink.admin.adapter.out.persistence.product;

import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(ProductJpaEntity.from(product)).toDomain();
    }

    @Override
    public List<Product> saveAll(List<Product> products) {
        return jpaRepository.saveAll(products.stream().map(ProductJpaEntity::from).toList())
                .stream().map(ProductJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Product> findByIdAndCompanyId(Long id, Long companyId) {
        return jpaRepository.findByIdAndCompanyId(id, companyId).map(ProductJpaEntity::toDomain);
    }

    @Override
    public Page<Product> search(Long companyId, String keyword, boolean enabledOnly, Pageable pageable) {
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return jpaRepository.search(companyId, normalized, enabledOnly, pageable).map(ProductJpaEntity::toDomain);
    }

    @Override
    public List<Product> findAllByBaseProductId(Long baseProductId) {
        return jpaRepository.findAllByBaseProductId(baseProductId).stream().map(ProductJpaEntity::toDomain).toList();
    }

    @Override
    public List<Long> findBaseProductIdsByCompanyId(Long companyId) {
        return jpaRepository.findBaseProductIdsByCompanyId(companyId);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByBaseProductId(Long baseProductId) {
        jpaRepository.deleteAllByBaseProductId(baseProductId);
    }
}
