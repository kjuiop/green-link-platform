package com.greenlink.admin.adapter.out.persistence.settlement;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.settlement.model.Settlement;
import com.greenlink.core.domain.settlement.model.SettlementType;
import jakarta.persistence.*;

@Entity
@Table(name = "settlements",
    uniqueConstraints = @UniqueConstraint(name = "uk_company_type_period",
        columnNames = {"company_id", "type", "from_date", "to_date"}))
public class SettlementJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementType type;

    @Column(nullable = false, length = 10)
    private String fromDate;

    @Column(nullable = false, length = 10)
    private String toDate;

    @Column(length = 30)
    private String completedAt;

    @Column
    private Long completedByAdminId;

    protected SettlementJpaEntity() {}

    public static SettlementJpaEntity from(Settlement s) {
        SettlementJpaEntity e = new SettlementJpaEntity();
        e.id = s.getId();
        e.companyId = s.getCompanyId();
        e.type = s.getType();
        e.fromDate = s.getFromDate();
        e.toDate = s.getToDate();
        e.completedAt = s.getCompletedAt();
        e.completedByAdminId = s.getCompletedByAdminId();
        return e;
    }

    public Settlement toDomain() {
        return Settlement.of(id, companyId, type, fromDate, toDate, completedAt, completedByAdminId);
    }
}
