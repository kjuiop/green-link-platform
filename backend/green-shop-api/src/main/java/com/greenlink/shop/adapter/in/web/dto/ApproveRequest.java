package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.domain.auth.ShopRole;

import java.util.Set;

public record ApproveRequest(ShopRole role, Set<Long> brandIds) {}
