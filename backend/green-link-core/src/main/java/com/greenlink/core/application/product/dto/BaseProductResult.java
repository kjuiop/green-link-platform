package com.greenlink.core.application.product.dto;

public record BaseProductResult(Long id, String name, long price, String memo, String createdAt, int sortOrder) {}
