package com.greenlink.admin.adapter.out.persistence.product;

import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.model.ProductCategory;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.greenlink.admin.adapter.out.persistence.product.QProductJpaEntity.productJpaEntity;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository, JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
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

    @Override
    public List<VendorMatch> findVendorMatches(ProductCategory category, Long baseProductId, String keyword) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(productJpaEntity.enabled.isTrue());
        where.and(productJpaEntity.companyId.isNotNull());
        if (category != null) where.and(productJpaEntity.category.eq(category));
        if (baseProductId != null) where.and(productJpaEntity.baseProductId.eq(baseProductId));
        if (keyword != null && !keyword.isBlank()) where.and(productJpaEntity.name.containsIgnoreCase(keyword.trim()));

        List<Tuple> rows = queryFactory
                .select(productJpaEntity.companyId, productJpaEntity.id.count(), productJpaEntity.price.min())
                .from(productJpaEntity)
                .where(where)
                .groupBy(productJpaEntity.companyId)
                .fetch();
        return rows.stream()
                .map(t -> new VendorMatch(t.get(productJpaEntity.companyId),
                        t.get(productJpaEntity.id.count()), t.get(productJpaEntity.price.min())))
                .toList();
    }
}
