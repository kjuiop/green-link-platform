package com.greenlink.core.application.common;

public final class BusinessNumber {

    private BusinessNumber() {}

    public static String normalize(String value) {
        if (value == null || value.isBlank()) return value;
        return value.replaceAll("[^0-9]", "");
    }
}
