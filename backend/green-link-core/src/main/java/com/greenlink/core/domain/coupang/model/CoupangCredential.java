package com.greenlink.core.domain.coupang.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CoupangCredential {

    public static final int KEY_VALID_DAYS = 180;

    private Long id;
    private Long companyId;
    private String vendorCode;
    private String accessKeyEnc;
    private String secretKeyEnc;
    private LocalDate keyIssuedAt;
    private LocalDate keyExpiresAt;
    private boolean enabled;
    private LocalDateTime lastCollectedAt;
    private String lastError;
    private LocalDateTime lastErrorAt;

    private CoupangCredential() {}

    public static CoupangCredential create(Long companyId, String vendorCode, String accessKeyEnc,
                                           String secretKeyEnc, LocalDate keyIssuedAt, boolean enabled) {
        CoupangCredential c = new CoupangCredential();
        c.companyId = companyId;
        c.updateKeys(vendorCode, accessKeyEnc, secretKeyEnc, keyIssuedAt, enabled);
        return c;
    }

    public static CoupangCredential of(Long id, Long companyId, String vendorCode, String accessKeyEnc,
                                       String secretKeyEnc, LocalDate keyIssuedAt, LocalDate keyExpiresAt,
                                       boolean enabled, LocalDateTime lastCollectedAt,
                                       String lastError, LocalDateTime lastErrorAt) {
        CoupangCredential c = new CoupangCredential();
        c.id = id;
        c.companyId = companyId;
        c.vendorCode = vendorCode;
        c.accessKeyEnc = accessKeyEnc;
        c.secretKeyEnc = secretKeyEnc;
        c.keyIssuedAt = keyIssuedAt;
        c.keyExpiresAt = keyExpiresAt;
        c.enabled = enabled;
        c.lastCollectedAt = lastCollectedAt;
        c.lastError = lastError;
        c.lastErrorAt = lastErrorAt;
        return c;
    }

    public void updateKeys(String vendorCode, String accessKeyEnc, String secretKeyEnc,
                           LocalDate keyIssuedAt, boolean enabled) {
        this.vendorCode = vendorCode;
        this.accessKeyEnc = accessKeyEnc;
        this.secretKeyEnc = secretKeyEnc;
        this.keyIssuedAt = keyIssuedAt;
        this.keyExpiresAt = keyIssuedAt.plusDays(KEY_VALID_DAYS);
        this.enabled = enabled;
        this.lastError = null;
        this.lastErrorAt = null;
    }

    public void recordSuccess(LocalDateTime collectedUntil) {
        this.lastCollectedAt = collectedUntil;
        this.lastError = null;
        this.lastErrorAt = null;
    }

    public void recordError(String message, LocalDateTime now) {
        this.lastError = message != null && message.length() > 500 ? message.substring(0, 500) : message;
        this.lastErrorAt = now;
    }

    public boolean isExpired(LocalDate today) {
        return keyExpiresAt != null && today.isAfter(keyExpiresAt);
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getVendorCode() { return vendorCode; }
    public String getAccessKeyEnc() { return accessKeyEnc; }
    public String getSecretKeyEnc() { return secretKeyEnc; }
    public LocalDate getKeyIssuedAt() { return keyIssuedAt; }
    public LocalDate getKeyExpiresAt() { return keyExpiresAt; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getLastCollectedAt() { return lastCollectedAt; }
    public String getLastError() { return lastError; }
    public LocalDateTime getLastErrorAt() { return lastErrorAt; }
}
