package com.greenlink.shopcore.domain.member.port;

import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

import java.util.List;
import java.util.Optional;

public interface ShopAdminMemberRepository {
    Optional<ShopAdminMember> findById(Long id);
    Optional<ShopAdminMember> findByOnurimAdminId(Long onurimAdminId);
    List<ShopAdminMember> findAll(ShopAdminStatus status);
    ShopAdminMember save(ShopAdminMember member);
}
