package com.greenlink.admin.adapter.out.persistence.product;

import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(ProductJpaEntity::toDomain);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(ProductJpaEntity::toDomain);
    }

    @Override
    public Page<Product> findAll(Pageable pageable, Long orderingCompanyId) {
        if (orderingCompanyId == null) {
            return jpaRepository.findAll(pageable).map(ProductJpaEntity::toDomain);
        }
        return jpaRepository.findByOrderingCompanyId(orderingCompanyId, pageable)
                .map(ProductJpaEntity::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll(Sort.by("name").ascending())
                .stream().map(ProductJpaEntity::toDomain).toList();
    }

    @Override
    public List<Product> findAll(Long orderingCompanyId) {
        if (orderingCompanyId == null) {
            return jpaRepository.findAll(Sort.by("name").ascending())
                    .stream().map(ProductJpaEntity::toDomain).toList();
        }
        return jpaRepository.findByOrderingCompanyIdOrderByNameAsc(orderingCompanyId)
                .stream().map(ProductJpaEntity::toDomain).toList();
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
