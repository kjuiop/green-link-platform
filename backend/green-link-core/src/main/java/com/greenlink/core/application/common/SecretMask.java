package com.greenlink.core.application.common;

public final class SecretMask {

    private static final String PREFIX = "****";

    private SecretMask() {}

    public static String mask(String secret) {
        if (secret == null || secret.isBlank()) return secret;
        String tail = secret.length() > 4 ? secret.substring(secret.length() - 4) : "";
        return PREFIX + tail;
    }

    public static boolean isMasked(String value) {
        return value != null && value.startsWith(PREFIX);
    }
}
