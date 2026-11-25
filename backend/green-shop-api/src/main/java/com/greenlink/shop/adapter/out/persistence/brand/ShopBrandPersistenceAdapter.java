package com.greenlink.shop.adapter.out.persistence.brand;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.domain.brand.port.ShopBrandRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class ShopBrandPersistenceAdapter implements ShopBrandRepository {

    private final ShopBrandJpaRepository jpaRepository;

    ShopBrandPersistenceAdapter(ShopBrandJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ShopBrand> findById(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id).map(ShopBrandJpaEntity::toDomain);
    }

    @Override
    public List<ShopBrand> findAll() {
        return jpaRepository.findByDeletedAtIsNullOrderByIdAsc().stream()
            .map(ShopBrandJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<ShopBrand> findAllByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyIdAndDeletedAtIsNullOrderByIdAsc(companyId).stream()
            .map(ShopBrandJpaEntity::toDomain)
            .toList();
    }

    @Override
    public boolean existsByCompanyIdAndName(Long companyId, String name) {
        return jpaRepository.existsByCompanyIdAndNameAndDeletedAtIsNull(companyId, name);
    }

    @Override
    public ShopBrand save(ShopBrand brand) {
        return jpaRepository.save(ShopBrandJpaEntity.from(brand)).toDomain();
    }
}
