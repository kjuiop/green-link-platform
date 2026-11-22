package com.greenlink.core.domain.shop.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ShopSlug {

    private static final Pattern FORMAT = Pattern.compile("^[a-z0-9](?:[a-z0-9-]{1,28}[a-z0-9])$");

    private static final Set<String> RESERVED = Set.of(
            "admin", "api", "www", "dev", "dev-api", "app", "login", "signup", "shop", "shops", "s",
            "public", "static", "assets", "orders", "settings", "coupang", "vendor", "mail", "help",
            "staging", "test", "cdn", "img", "images", "onurim");

    private ShopSlug() {}

    /** 소문자로 정규화한 뒤 형식·예약어를 검증한다. */
    public static String normalize(String raw) {
        String s = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (!FORMAT.matcher(s).matches() || s.contains("--") || RESERVED.contains(s)) {
            throw new BusinessException(ShopErrorCode.INVALID_SLUG);
        }
        return s;
    }

    /** 공개 조회용: 형식이 맞지 않으면 예외 대신 null을 돌려 404로 이어지게 한다. */
    public static String normalizeOrNull(String raw) {
        try {
            return normalize(raw);
        } catch (BusinessException e) {
            return null;
        }
    }
}
