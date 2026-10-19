package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.core.application.order.dto.OrderSearchCondition;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public OrderQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<OrderJpaEntity> search(OrderSearchCondition condition) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        BooleanBuilder where = new BooleanBuilder();

        if (condition.status() != null && !condition.status().isBlank()) {
            where.and(order.status.eq(OrderStatus.valueOf(condition.status().toUpperCase())));
        }
        if (condition.vendorId() != null) {
            where.and(order.vendorId.eq(condition.vendorId()));
        }
        if (condition.orderNumber() != null && !condition.orderNumber().isBlank()) {
            where.and(order.orderNumber.containsIgnoreCase(condition.orderNumber()));
        }
        if (condition.productName() != null && !condition.productName().isBlank()) {
            where.and(order.productName.containsIgnoreCase(condition.productName()));
        }
        if (condition.ordererName() != null && !condition.ordererName().isBlank()) {
            where.and(order.ordererName.containsIgnoreCase(condition.ordererName()));
        }
        if (condition.deliveryDateFrom() != null && !condition.deliveryDateFrom().isBlank()) {
            where.and(order.deliveryDate.goe(condition.deliveryDateFrom()));
        }
        if (condition.deliveryDateTo() != null && !condition.deliveryDateTo().isBlank()) {
            where.and(order.deliveryDate.loe(condition.deliveryDateTo()));
        }

        return queryFactory
                .selectFrom(order)
                .where(where)
                .orderBy(order.createdAt.desc())
                .fetch();
    }
}
