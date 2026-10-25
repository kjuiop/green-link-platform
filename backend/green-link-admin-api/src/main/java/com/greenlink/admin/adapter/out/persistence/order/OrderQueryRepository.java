package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.admin.adapter.out.persistence.receipt.QReceiptConfirmationJpaEntity;
import com.greenlink.admin.adapter.out.persistence.sms.QSmsMessageJpaEntity;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public OrderQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<OrderJpaEntity> search(OrderSearchCondition condition) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        return queryFactory
                .selectFrom(order)
                .where(buildWhere(condition, order))
                .orderBy(order.deliveryDate.desc(), order.createdAt.desc())
                .fetch();
    }

    public Page<OrderJpaEntity> searchPaged(OrderSearchCondition condition, Pageable pageable) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        BooleanBuilder where = buildWhere(condition, order);

        List<OrderJpaEntity> content = queryFactory
                .selectFrom(order)
                .where(where)
                .orderBy(order.deliveryDate.desc(), order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildWhere(OrderSearchCondition condition, QOrderJpaEntity order) {
        BooleanBuilder where = new BooleanBuilder();

        if (condition.status() != null && !condition.status().isBlank()) {
            OrderStatus orderStatus = Arrays.stream(OrderStatus.values())
                    .filter(s -> s.name().equalsIgnoreCase(condition.status()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.INVALID_ORDER_STATUS));
            where.and(order.status.eq(orderStatus));
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
        if (condition.orderingCompanyId() != null) {
            where.and(order.orderingCompanyId.eq(condition.orderingCompanyId()));
        }
        if (condition.receiverName() != null && !condition.receiverName().isBlank()) {
            QReceiptConfirmationJpaEntity receipt = QReceiptConfirmationJpaEntity.receiptConfirmationJpaEntity;
            where.and(JPAExpressions.selectOne()
                    .from(receipt)
                    .where(receipt.orderId.eq(order.id)
                            .and(receipt.receiverName.containsIgnoreCase(condition.receiverName())))
                    .exists());
        }
        if (condition.ribbonLeft() != null && !condition.ribbonLeft().isBlank()) {
            where.and(order.ribbonLeft.containsIgnoreCase(condition.ribbonLeft()));
        }
        if (condition.messageStatus() != null && !condition.messageStatus().isBlank()) {
            QSmsMessageJpaEntity sms = QSmsMessageJpaEntity.smsMessageJpaEntity;
            switch (condition.messageStatus()) {
                case "NONE" -> where.and(JPAExpressions.selectOne()
                        .from(sms).where(sms.orderId.eq(order.id)).notExists());
                case "SENT" -> where.and(JPAExpressions.selectOne()
                        .from(sms)
                        .where(sms.orderId.eq(order.id)
                                .and(sms.status.eq(SmsStatus.SUCCESS)))
                        .exists());
                case "FAIL" -> {
                    where.and(JPAExpressions.selectOne()
                            .from(sms).where(sms.orderId.eq(order.id)).exists());
                    where.and(JPAExpressions.selectOne()
                            .from(sms)
                            .where(sms.orderId.eq(order.id)
                                    .and(sms.status.eq(SmsStatus.SUCCESS)))
                            .notExists());
                }
            }
        }

        return where;
    }
}
