package com.greenlink.admin.adapter.out.persistence.admin;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.admin.model.AdminStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "admins")
public class AdminJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "company_id")
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminStatus status = AdminStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'PARTNER_ADMIN'")
    private AdminRole role = AdminRole.PARTNER_ADMIN;

    @Column(name = "onurim_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean onurimEnabled = true;

    @Column(length = 30)
    private String deletedAt;

    @Column(name = "password_setup_expires_at")
    private LocalDateTime passwordSetupExpiresAt;

    @Column(name = "password_setup_attempts", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int passwordSetupAttempts;

    protected AdminJpaEntity() {}

    public static AdminJpaEntity from(Admin admin) {
        AdminJpaEntity e = new AdminJpaEntity();
        e.id = admin.getId();
        e.loginId = admin.getLoginId();
        e.password = admin.getPassword();
        e.name = admin.getName();
        e.phone = admin.getPhone();
        e.email = admin.getEmail();
        e.companyId = admin.getCompanyId();
        e.status = admin.getStatus() != null ? admin.getStatus() : AdminStatus.ACTIVE;
        e.role = admin.getRole() != null ? admin.getRole() : AdminRole.PARTNER_ADMIN;
        e.onurimEnabled = admin.isOnurimEnabled();
        e.deletedAt = admin.getDeletedAt();
        e.passwordSetupExpiresAt = admin.getPasswordSetupExpiresAt();
        e.passwordSetupAttempts = admin.getPasswordSetupAttempts();
        return e;
    }

    public Admin toDomain() {
        String createdAtStr = getCreatedAt() != null
                ? getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        Admin admin = Admin.of(id, loginId, password, name, phone, email, companyId, status, createdAtStr, deletedAt);
        admin.restoreAccess(role, onurimEnabled);
        admin.restorePasswordSetup(passwordSetupExpiresAt, passwordSetupAttempts);
        return admin;
    }
}
