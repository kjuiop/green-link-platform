package com.greenlink.admin.adapter.out.persistence.settlement;

import com.greenlink.admin.adapter.out.persistence.order.QOrderJpaEntity;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.settlement.port.SettlementQueryPort;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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
        NumberExpression<Long> orderTotal = orderTotal(o);
        NumberExpression<Long> effectiveVendorAmount = effectiveVendorAmount(o);
        return queryFactory
                .select(o.vendorCompanyId,
                        o.count(),
                        orderTotal.sum(),
                        effectiveVendorAmount.sum())
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
                        nvl(t.get(orderTotal.sum())),
                        nvl(t.get(effectiveVendorAmount.sum()))
                ))
                .toList();
    }

    @Override
    public List<CompanySettlementRaw> sumByOrderingCompany(String fromDate, String toDate) {
        QOrderJpaEntity o = QOrderJpaEntity.orderJpaEntity;
        NumberExpression<Long> orderTotal = orderTotal(o);
        return queryFactory
                .select(o.orderingCompanyId,
                        o.count(),
                        orderTotal.sum(),
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
                        nvl(t.get(orderTotal.sum())),
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
                .map(e -> { Order o2 = e.toDomain(); return toRaw(o2, o2.getOrderingCompanyId(), o2.getAmount()); })
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
                .map(e -> { Order o2 = e.toDomain(); return toRaw(o2, o2.getVendorCompanyId(), o2.getAmount()); })
                .toList();
    }

    private static OrderSettlementRaw toRaw(Order o, Long counterpartCompanyId, long orderAmount) {
        return new OrderSettlementRaw(
                o.getId(), o.getOrderNumber(), counterpartCompanyId,
                o.getProductName(), o.getPlaceName(), o.getOrdererName(),
                o.getCompletedAt(), orderAmount, o.getVendorAmount(),
                o.getExtraCost(), o.getVendorFee(), o.getAdjustAmount()
        );
    }

    /** 발주금액 = 수주금액 = 주문금액 + 추가비용 + 조정금액 */
    private static NumberExpression<Long> orderTotal(QOrderJpaEntity o) {
        return o.amount.add(o.extraCost.coalesce(0L)).add(o.adjustAmount.coalesce(0L));
    }

    /** 수주사 정산금액: vendorAmount 스냅샷 우선, null이면 발주금액 − vendorFee */
    private static NumberExpression<Long> effectiveVendorAmount(QOrderJpaEntity o) {
        return new CaseBuilder()
                .when(o.vendorAmount.isNotNull()).then(o.vendorAmount)
                .otherwise(orderTotal(o).subtract(o.vendorFee.coalesce(0L)));
    }

    private static long nvl(Long v) { return v != null ? v : 0L; }
}
