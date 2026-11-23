package com.greenlink.shopcore.application.admin.usecase;

import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

import java.util.List;

public interface ShopAdminManagementUseCase {
    List<ShopAdminView> list(ShopAdminStatus status);
    void approve(Long id, Long actorAdminId);
    void reject(Long id, String reason, Long actorAdminId);
}
