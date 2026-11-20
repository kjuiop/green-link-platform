package com.greenlink.core.application.common;

public final class SecretMask {

    private static final String PREFIX = "****";

    private SecretMask() {}

    public static String mask(String secret) {
        if (secret == null || secret.isBlank()) return secret;
        String tail = secret.length() > 4 ? secret.substring(secret.length() - 4) : "";
        return PREFIX + tail;
    }

    /** 마스킹된 값이 오면 같은 바로빌 계정일 때만 기존 키를 유지하고, 제공사가 바뀐 경우엔 비운다. */
    public static String resolveKey(String incoming, String existing, String existingProvider, String newProvider) {
        if (!isMasked(incoming)) return incoming;
        boolean sameBarobill = "BAROBILL".equalsIgnoreCase(existingProvider) && "BAROBILL".equalsIgnoreCase(newProvider);
        return sameBarobill ? existing : null;
    }

    public static boolean isMasked(String value) {
        return value != null && value.startsWith(PREFIX);
    }
}
