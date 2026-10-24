package com.greenlink.core.domain.phoneverification.model;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class PhoneVerification {

    private Long id;
    private String phone;
    private String code;
    private LocalDateTime expiresAt;
    private boolean verified;
    private boolean used;
    private LocalDateTime createdAt;

    private PhoneVerification() {}

    public static PhoneVerification create(String phone) {
        PhoneVerification v = new PhoneVerification();
        v.phone = phone;
        v.code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        v.expiresAt = LocalDateTime.now().plusMinutes(3);
        v.verified = false;
        v.used = false;
        return v;
    }

    public static PhoneVerification of(Long id, String phone, String code,
                                       LocalDateTime expiresAt, boolean verified,
                                       boolean used, LocalDateTime createdAt) {
        PhoneVerification v = new PhoneVerification();
        v.id = id;
        v.phone = phone;
        v.code = code;
        v.expiresAt = expiresAt;
        v.verified = verified;
        v.used = used;
        v.createdAt = createdAt;
        return v;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matchesCode(String inputCode) {
        return !used && !isExpired() && code.equals(inputCode);
    }

    public void markVerified() {
        this.verified = true;
    }

    public void markUsed() {
        this.used = true;
    }

    public boolean isVerifiedAndAvailable() {
        return verified && !used && !isExpired();
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getCode() { return code; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isVerified() { return verified; }
    public boolean isUsed() { return used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
