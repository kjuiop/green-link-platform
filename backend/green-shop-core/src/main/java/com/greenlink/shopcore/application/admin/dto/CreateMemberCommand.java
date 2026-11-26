package com.greenlink.shopcore.application.admin.dto;

import com.greenlink.shopcore.domain.auth.ShopRole;

import java.util.Set;

public record CreateMemberCommand(String loginId, String name, String phone, String email, ShopRole role,
                                  Long companyId, Set<Long> brandIds) {}
