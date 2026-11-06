package com.greenlink.core.application.product.dto;

public record CreateProductCommand(String name, long price, String memo) {}
