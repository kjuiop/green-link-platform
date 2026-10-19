package com.greenlink.admin.adapter.out.persistence.admin;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import jakarta.persistence.*;

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

    @Column(name = "company_id")
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminStatus status = AdminStatus.ACTIVE;

    protected AdminJpaEntity() {}

    public static AdminJpaEntity from(Admin admin) {
        AdminJpaEntity e = new AdminJpaEntity();
        e.id = admin.getId();
        e.loginId = admin.getLoginId();
        e.password = admin.getPassword();
        e.name = admin.getName();
        e.phone = admin.getPhone();
        e.companyId = admin.getCompanyId();
        e.status = admin.getStatus() != null ? admin.getStatus() : AdminStatus.ACTIVE;
        return e;
    }

    public Admin toDomain() {
        return Admin.of(id, loginId, password, name, phone, companyId, status);
    }
}
