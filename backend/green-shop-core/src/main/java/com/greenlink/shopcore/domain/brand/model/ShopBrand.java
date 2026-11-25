package com.greenlink.shopcore.domain.brand.model;

import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;

import java.time.LocalDateTime;

public class ShopBrand {

    private static final int NAME_MAX_LENGTH = 100;

    private Long id;
    private Long companyId;
    private String name;
    private LocalDateTime deletedAt;

    private ShopBrand() {}

    public static ShopBrand create(Long companyId, String name) {
        ShopBrand b = new ShopBrand();
        b.companyId = companyId;
        b.name = normalize(name);
        return b;
    }

    public static ShopBrand of(Long id, Long companyId, String name, LocalDateTime deletedAt) {
        ShopBrand b = new ShopBrand();
        b.id = id;
        b.companyId = companyId;
        b.name = name;
        b.deletedAt = deletedAt;
        return b;
    }

    public void rename(String newName) {
        this.name = normalize(newName);
    }

    public void delete(LocalDateTime now) {
        if (deletedAt == null) {
            this.deletedAt = now;
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public static String normalize(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty() || trimmed.length() > NAME_MAX_LENGTH) {
            throw new ShopBusinessException(ShopErrorCode.BRAND_NAME_INVALID);
        }
        return trimmed;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getName() { return name; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
