package com.greenlink.shopcore.domain.member.port;

import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

import java.util.List;
import java.util.Optional;

public interface ShopAdminMemberRepository {
    Optional<ShopAdminMember> findById(Long id);
    Optional<ShopAdminMember> findByAdminId(Long adminId);
    List<ShopAdminMember> findAll(ShopAdminStatus status);
    boolean existsApprovedByCompanyId(Long companyId);
    ShopAdminMember save(ShopAdminMember member);
}
