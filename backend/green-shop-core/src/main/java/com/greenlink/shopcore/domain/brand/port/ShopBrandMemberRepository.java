package com.greenlink.shopcore.domain.brand.port;

import com.greenlink.shopcore.domain.brand.model.ShopBrandMember;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ShopBrandMemberRepository {
    Set<Long> findBrandIdsByMemberId(Long memberId);
    Map<Long, Set<Long>> findBrandIdsByMemberIds(Collection<Long> memberIds);
    boolean exists(Long brandId, Long memberId);
    ShopBrandMember save(ShopBrandMember member);
    void replaceAssignments(Long memberId, Set<Long> brandIds, LocalDateTime now);
    void unassignAllByMemberId(Long memberId, LocalDateTime now);
    void unassignAllByBrandId(Long brandId, LocalDateTime now);
}
