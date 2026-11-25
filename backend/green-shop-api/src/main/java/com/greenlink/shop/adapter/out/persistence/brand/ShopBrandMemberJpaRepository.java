package com.greenlink.shop.adapter.out.persistence.brand;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

interface ShopBrandMemberJpaRepository extends JpaRepository<ShopBrandMemberJpaEntity, Long> {
    List<ShopBrandMemberJpaEntity> findByMemberId(Long memberId);
    List<ShopBrandMemberJpaEntity> findByMemberIdAndDeletedAtIsNull(Long memberId);
    List<ShopBrandMemberJpaEntity> findByMemberIdInAndDeletedAtIsNull(Collection<Long> memberIds);
    boolean existsByBrandIdAndMemberIdAndDeletedAtIsNull(Long brandId, Long memberId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ShopBrandMemberJpaEntity e set e.deletedAt = :now where e.memberId = :memberId and e.deletedAt is null")
    void softDeleteByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ShopBrandMemberJpaEntity e set e.deletedAt = :now where e.brandId = :brandId and e.deletedAt is null")
    void softDeleteByBrandId(@Param("brandId") Long brandId, @Param("now") LocalDateTime now);
}
