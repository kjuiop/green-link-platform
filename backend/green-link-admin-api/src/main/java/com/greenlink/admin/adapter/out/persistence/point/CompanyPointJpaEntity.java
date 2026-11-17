package com.greenlink.admin.adapter.out.persistence.point;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.point.model.CompanyPoint;
import jakarta.persistence.*;

@Entity
@Table(name = "company_points",
    uniqueConstraints = @UniqueConstraint(name = "uk_company_points_company_id", columnNames = "company_id"))
public class CompanyPointJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private long balance;

    protected CompanyPointJpaEntity() {}

    public static CompanyPointJpaEntity from(CompanyPoint p) {
        CompanyPointJpaEntity e = new CompanyPointJpaEntity();
        e.id = p.getId();
        e.companyId = p.getCompanyId();
        e.balance = p.getBalance();
        return e;
    }

    public CompanyPoint toDomain() {
        return CompanyPoint.of(id, companyId, balance);
    }
}
