package com.greenlink.core.application.product.dto;

import com.greenlink.core.domain.product.model.ProductCategory;

public record ProductCommand(String name, long price, String memo, int sortOrder, ProductCategory category) {}
