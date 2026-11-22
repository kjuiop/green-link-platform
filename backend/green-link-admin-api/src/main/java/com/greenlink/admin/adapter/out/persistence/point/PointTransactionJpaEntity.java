package com.greenlink.admin.adapter.out.persistence.point;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.model.PointTransactionType;
import jakarta.persistence.*;

import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "point_transactions",
    indexes = {
        @Index(name = "idx_point_tx_company_created", columnList = "company_id, created_at"),
        @Index(name = "idx_point_tx_order", columnList = "order_id")
    })
public class PointTransactionJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column
    private Long memberId;

    @Column
    private Long adminId;

    @Column
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long balanceAfter;

    @Column(length = 255)
    private String memo;

    protected PointTransactionJpaEntity() {}

    public static PointTransactionJpaEntity from(PointTransaction t) {
        PointTransactionJpaEntity e = new PointTransactionJpaEntity();
        e.id = t.getId();
        e.companyId = t.getCompanyId();
        e.memberId = t.getMemberId();
        e.adminId = t.getAdminId();
        e.orderId = t.getOrderId();
        e.type = t.getType();
        e.amount = t.getAmount();
        e.balanceAfter = t.getBalanceAfter();
        e.memo = t.getMemo();
        return e;
    }

    public PointTransaction toDomain() {
        String createdAtStr = getCreatedAt() != null
                ? getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        return PointTransaction.of(id, companyId, memberId, adminId, orderId, type, amount, balanceAfter, memo, createdAtStr);
    }
}
