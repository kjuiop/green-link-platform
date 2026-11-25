package com.greenlink.shopcore.application.admin.usecase;

import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

import java.util.List;
import java.util.Set;

public interface ShopAdminManagementUseCase {
    List<ShopAdminView> list(ManagementActor actor, ShopAdminStatus status);
    void approve(ManagementActor actor, Long id, ShopRole grantedRole, Set<Long> brandIds);
    void reject(ManagementActor actor, Long id, String reason);
    void changeRole(ManagementActor actor, Long id, ShopRole newRole);
    void assignBrands(ManagementActor actor, Long id, Set<Long> brandIds);
    void remove(ManagementActor actor, Long id);
}
