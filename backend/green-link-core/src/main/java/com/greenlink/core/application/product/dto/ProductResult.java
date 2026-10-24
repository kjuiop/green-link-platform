package com.greenlink.core.application.product.dto;

public record ProductResult(Long id, String name, long price, String memo, String createdAt, Long orderingCompanyId) {}
