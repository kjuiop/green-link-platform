package com.greenlink.shop.adapter.out.persistence.brand;

import com.greenlink.shopcore.domain.brand.model.ShopBrandMember;
import com.greenlink.shopcore.domain.brand.port.ShopBrandMemberRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
class ShopBrandMemberPersistenceAdapter implements ShopBrandMemberRepository {

    private final ShopBrandMemberJpaRepository jpaRepository;

    ShopBrandMemberPersistenceAdapter(ShopBrandMemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Set<Long> findBrandIdsByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId).stream()
            .map(ShopBrandMemberJpaEntity::getBrandId)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean exists(Long brandId, Long memberId) {
        return jpaRepository.existsByBrandIdAndMemberId(brandId, memberId);
    }

    @Override
    public ShopBrandMember save(ShopBrandMember member) {
        return jpaRepository.save(ShopBrandMemberJpaEntity.from(member)).toDomain();
    }
}
