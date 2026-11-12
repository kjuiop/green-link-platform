package com.greenlink.admin.adapter.out.persistence.settlement;

import com.greenlink.admin.adapter.out.persistence.order.QOrderJpaEntity;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.settlement.port.SettlementQueryPort;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SettlementQueryRepository implements SettlementQueryPort {

    private final JPAQueryFactory queryFactory;

    public SettlementQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<CompanySettlementRaw> sumByVendorCompany(String fromDate, String toDate) {
        QOrderJpaEntity o = QOrderJpaEntity.orderJpaEntity;
        return queryFactory
                .select(o.vendorCompanyId,
                        o.count(),
                        o.amount.sum(),
                        o.vendorAmount.coalesce(0L).sum())
                .from(o)
                .where(o.status.eq(OrderStatus.DELIVERED)
                        .and(o.completedAt.goe(fromDate))
                        .and(o.completedAt.loe(toDate + "T23:59:59"))
                        .and(o.isDeleted.isFalse())
                        .and(o.vendorCompanyId.isNotNull()))
                .groupBy(o.vendorCompanyId)
                .fetch()
                .stream()
                .map(t -> new CompanySettlementRaw(
                        t.get(o.vendorCompanyId),
                        nvl(t.get(o.count())),
                        nvl(t.get(o.amount.sum())),
                        nvl(t.get(o.vendorAmount.coalesce(0L).sum()))
                ))
                .toList();
    }

    @Override
    public List<CompanySettlementRaw> sumByOrderingCompany(String fromDate, String toDate) {
        QOrderJpaEntity o = QOrderJpaEntity.orderJpaEntity;
        return queryFactory
                .select(o.orderingCompanyId,
                        o.count(),
                        o.amount.sum(),
                        o.vendorAmount.coalesce(0L).sum())
                .from(o)
                .where(o.status.eq(OrderStatus.DELIVERED)
                        .and(o.completedAt.goe(fromDate))
                        .and(o.completedAt.loe(toDate + "T23:59:59"))
                        .and(o.isDeleted.isFalse())
                        .and(o.orderingCompanyId.isNotNull()))
                .groupBy(o.orderingCompanyId)
                .fetch()
                .stream()
                .map(t -> new CompanySettlementRaw(
                        t.get(o.orderingCompanyId),
                        nvl(t.get(o.count())),
                        nvl(t.get(o.amount.sum())),
                        nvl(t.get(o.vendorAmount.coalesce(0L).sum()))
                ))
                .toList();
    }

    @Override
    public List<OrderSettlementRaw> findDeliveredOrdersByVendorCompany(Long vendorCompanyId, String fromDate, String toDate) {
        QOrderJpaEntity o = QOrderJpaEntity.orderJpaEntity;
        return queryFactory
                .selectFrom(o)
                .where(o.status.eq(OrderStatus.DELIVERED)
                        .and(o.vendorCompanyId.eq(vendorCompanyId))
                        .and(o.completedAt.goe(fromDate))
                        .and(o.completedAt.loe(toDate + "T23:59:59"))
                        .and(o.isDeleted.isFalse()))
                .orderBy(o.completedAt.asc())
                .fetch()
                .stream()
                .map(e -> toRaw(e.toDomain(), e.toDomain().getOrderingCompanyId()))
                .toList();
    }

    @Override
    public List<OrderSettlementRaw> findDeliveredOrdersByOrderingCompany(Long orderingCompanyId, String fromDate, String toDate) {
        QOrderJpaEntity o = QOrderJpaEntity.orderJpaEntity;
        return queryFactory
                .selectFrom(o)
                .where(o.status.eq(OrderStatus.DELIVERED)
                        .and(o.orderingCompanyId.eq(orderingCompanyId))
                        .and(o.completedAt.goe(fromDate))
                        .and(o.completedAt.loe(toDate + "T23:59:59"))
                        .and(o.isDeleted.isFalse()))
                .orderBy(o.completedAt.asc())
                .fetch()
                .stream()
                .map(e -> toRaw(e.toDomain(), e.toDomain().getVendorCompanyId()))
                .toList();
    }

    private static OrderSettlementRaw toRaw(Order o, Long counterpartCompanyId) {
        return new OrderSettlementRaw(
                o.getId(), o.getOrderNumber(), counterpartCompanyId,
                o.getProductName(), o.getPlaceName(), o.getOrdererName(),
                o.getCompletedAt(), o.getAmount(), o.getVendorAmount()
        );
    }

    private static long nvl(Long v) { return v != null ? v : 0L; }
}
