package com.greenlink.admin.adapter.out.persistence.product;

import com.greenlink.core.domain.product.model.BaseProduct;
import com.greenlink.core.domain.product.port.BaseProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class BaseProductRepositoryAdapter implements BaseProductRepository {

    private final BaseProductJpaRepository jpaRepository;

    public BaseProductRepositoryAdapter(BaseProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BaseProduct save(BaseProduct product) {
        return jpaRepository.save(BaseProductJpaEntity.from(product)).toDomain();
    }

    @Override
    public Optional<BaseProduct> findById(Long id) {
        return jpaRepository.findById(id).map(BaseProductJpaEntity::toDomain);
    }

    @Override
    public Page<BaseProduct> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(BaseProductJpaEntity::toDomain);
    }

    @Override
    public List<BaseProduct> findAll() {
        return jpaRepository.findAll(Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()))
                .stream().map(BaseProductJpaEntity::toDomain).toList();
    }

    @Override
    public List<BaseProduct> findAllByIdIn(Collection<Long> ids) {
        return jpaRepository.findAllById(ids).stream().map(BaseProductJpaEntity::toDomain).toList();
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }
}
