package com.greenlink.shopcore.domain.brand.port;

import com.greenlink.shopcore.domain.brand.model.ShopBrandMember;

import java.util.Set;

public interface ShopBrandMemberRepository {
    Set<Long> findBrandIdsByMemberId(Long memberId);
    boolean exists(Long brandId, Long memberId);
    ShopBrandMember save(ShopBrandMember member);
}
