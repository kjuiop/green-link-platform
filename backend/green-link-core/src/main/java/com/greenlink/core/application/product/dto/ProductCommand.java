package com.greenlink.core.application.product.dto;

public record ProductCommand(String name, long price, String memo, int sortOrder) {}
