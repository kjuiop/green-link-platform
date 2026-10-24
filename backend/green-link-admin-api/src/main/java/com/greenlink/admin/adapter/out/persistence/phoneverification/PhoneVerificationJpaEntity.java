package com.greenlink.admin.adapter.out.persistence.phoneverification;

import com.greenlink.core.domain.phoneverification.model.PhoneVerification;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_verifications")
public class PhoneVerificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    protected PhoneVerificationJpaEntity() {}

    public static PhoneVerificationJpaEntity from(PhoneVerification v) {
        PhoneVerificationJpaEntity e = new PhoneVerificationJpaEntity();
        e.id = v.getId();
        e.phone = v.getPhone();
        e.code = v.getCode();
        e.expiresAt = v.getExpiresAt();
        e.verified = v.isVerified();
        e.used = v.isUsed();
        e.createdAt = v.getCreatedAt();
        return e;
    }

    public PhoneVerification toDomain() {
        return PhoneVerification.of(id, phone, code, expiresAt, verified, used, createdAt);
    }
}
