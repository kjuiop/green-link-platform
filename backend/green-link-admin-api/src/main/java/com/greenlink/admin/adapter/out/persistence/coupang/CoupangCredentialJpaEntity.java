package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.coupang.model.CoupangCredential;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupang_credentials")
public class CoupangCredentialJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long companyId;

    @Column(nullable = false, length = 50)
    private String vendorCode;

    @Column(nullable = false, length = 500)
    private String accessKeyEnc;

    @Column(nullable = false, length = 500)
    private String secretKeyEnc;

    @Column(nullable = false)
    private LocalDate keyIssuedAt;

    @Column(nullable = false)
    private LocalDate keyExpiresAt;

    @Column(nullable = false)
    private boolean enabled;

    private LocalDateTime lastCollectedAt;

    @Column(length = 500)
    private String lastError;

    private LocalDateTime lastErrorAt;

    protected CoupangCredentialJpaEntity() {}

    static CoupangCredentialJpaEntity from(CoupangCredential c) {
        CoupangCredentialJpaEntity e = new CoupangCredentialJpaEntity();
        e.companyId = c.getCompanyId();
        e.apply(c);
        return e;
    }

    void apply(CoupangCredential c) {
        this.vendorCode = c.getVendorCode();
        this.accessKeyEnc = c.getAccessKeyEnc();
        this.secretKeyEnc = c.getSecretKeyEnc();
        this.keyIssuedAt = c.getKeyIssuedAt();
        this.keyExpiresAt = c.getKeyExpiresAt();
        this.enabled = c.isEnabled();
        this.lastCollectedAt = c.getLastCollectedAt();
        this.lastError = c.getLastError();
        this.lastErrorAt = c.getLastErrorAt();
    }

    CoupangCredential toDomain() {
        return CoupangCredential.of(id, companyId, vendorCode, accessKeyEnc, secretKeyEnc, keyIssuedAt,
                keyExpiresAt, enabled, lastCollectedAt, lastError, lastErrorAt);
    }
}
