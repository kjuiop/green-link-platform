package com.greenlink.shop.adapter.out.persistence.brand;

import com.greenlink.shopcore.domain.brand.model.ShopBrandMember;
import com.greenlink.shopcore.domain.brand.port.ShopBrandMemberRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        return jpaRepository.findByMemberIdAndDeletedAtIsNull(memberId).stream()
            .map(ShopBrandMemberJpaEntity::getBrandId)
            .collect(Collectors.toSet());
    }

    @Override
    public Map<Long, Set<Long>> findBrandIdsByMemberIds(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Map.of();
        }
        return jpaRepository.findByMemberIdInAndDeletedAtIsNull(memberIds).stream()
            .collect(Collectors.groupingBy(ShopBrandMemberJpaEntity::getMemberId,
                Collectors.mapping(ShopBrandMemberJpaEntity::getBrandId, Collectors.toSet())));
    }

    @Override
    public boolean exists(Long brandId, Long memberId) {
        return jpaRepository.existsByBrandIdAndMemberIdAndDeletedAtIsNull(brandId, memberId);
    }

    @Override
    public ShopBrandMember save(ShopBrandMember member) {
        return jpaRepository.save(ShopBrandMemberJpaEntity.from(member)).toDomain();
    }

    @Override
    public void replaceAssignments(Long memberId, Set<Long> brandIds, LocalDateTime now) {
        List<ShopBrandMemberJpaEntity> rows = jpaRepository.findByMemberId(memberId);
        Set<Long> existing = new HashSet<>();
        for (ShopBrandMemberJpaEntity row : rows) {
            existing.add(row.getBrandId());
            if (brandIds.contains(row.getBrandId())) {
                row.restore();
            } else {
                row.softDelete(now);
            }
        }
        for (Long brandId : brandIds) {
            if (!existing.contains(brandId)) {
                jpaRepository.save(ShopBrandMemberJpaEntity.from(ShopBrandMember.assign(brandId, memberId)));
            }
        }
    }

    @Override
    public void unassignAllByMemberId(Long memberId, LocalDateTime now) {
        jpaRepository.softDeleteByMemberId(memberId, now);
    }

    @Override
    public void unassignAllByBrandId(Long brandId, LocalDateTime now) {
        jpaRepository.softDeleteByBrandId(brandId, now);
    }
}
