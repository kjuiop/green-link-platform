package com.greenlink.shop.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BrandAssignRequest(@NotNull Set<Long> brandIds) {}
