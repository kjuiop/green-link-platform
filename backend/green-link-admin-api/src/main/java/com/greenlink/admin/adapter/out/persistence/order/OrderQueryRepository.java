package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.admin.adapter.out.persistence.admin.QAdminJpaEntity;
import com.greenlink.admin.adapter.out.persistence.company.QCompanyJpaEntity;
import com.greenlink.admin.adapter.out.persistence.photo.QPhotoJpaEntity;
import com.greenlink.admin.adapter.out.persistence.receipt.QReceiptConfirmationJpaEntity;
import com.greenlink.admin.adapter.out.persistence.sms.QSmsMessageJpaEntity;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
                .orderBy(buildOrderBy(condition, order))
                .fetch();
    }

    public Page<OrderJpaEntity> searchPaged(OrderSearchCondition condition, Pageable pageable) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        BooleanBuilder where = buildWhere(condition, order);

        List<OrderJpaEntity> content = queryFactory
                .selectFrom(order)
                .where(where)
                .orderBy(buildOrderBy(condition, order))
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

    public long sumAmount(OrderSearchCondition condition) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        BooleanBuilder where = buildWhere(condition, order);
        Long amountSum = queryFactory.select(order.amount.sum()).from(order).where(where).fetchOne();
        Long extraCostSum = queryFactory.select(order.extraCost.sum()).from(order).where(where).fetchOne();
        return (amountSum != null ? amountSum : 0L) + (extraCostSum != null ? extraCostSum : 0L);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private OrderSpecifier<?>[] buildOrderBy(OrderSearchCondition condition, QOrderJpaEntity order) {
        if ("messageSentAt".equals(condition.sortBy())) {
            QSmsMessageJpaEntity sms = QSmsMessageJpaEntity.smsMessageJpaEntity;
            Expression<String> maxSentAt = (Expression<String>) JPAExpressions
                    .select(sms.sentAt.max())
                    .from(sms)
                    .where(sms.orderId.eq(order.id).and(sms.status.eq(SmsStatus.SUCCESS)));
            boolean desc = "desc".equalsIgnoreCase(condition.sortDir());
            return new OrderSpecifier[]{
                new OrderSpecifier(
                    desc ? com.querydsl.core.types.Order.DESC : com.querydsl.core.types.Order.ASC,
                    maxSentAt,
                    OrderSpecifier.NullHandling.NullsLast
                )
            };
        }
        return new OrderSpecifier[]{priorityGroup(order), order.createdAt.desc()};
    }

    private OrderSpecifier<Integer> priorityGroup(QOrderJpaEntity order) {
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String threeHoursAgoStr = now.minusHours(3)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        QPhotoJpaEntity photo = QPhotoJpaEntity.photoJpaEntity;

        // 결혼식 지연: 예약발송 시각 경과 + 현장사진 없음
        com.querydsl.core.types.dsl.StringExpression weddingDeadline =
                com.querydsl.core.types.dsl.Expressions.stringTemplate(
                    "CONCAT({0}, ' ', LPAD({1}, 2, '0'), ':', LPAD({2}, 2, '0'))",
                    order.deliveryDate, order.scheduledMessageHour, order.scheduledMessageMinute);

        com.querydsl.core.types.dsl.BooleanExpression noSitePhoto =
                JPAExpressions.selectOne()
                    .from(photo)
                    .where(photo.orderId.eq(order.id)
                        .and(photo.photoType.eq(com.greenlink.core.domain.photo.model.PhotoType.SITE)))
                    .notExists();

        com.querydsl.core.types.dsl.BooleanExpression isWeddingDelayed =
                order.status.eq(OrderStatus.ACCEPTED)
                    .and(order.eventType.eq("결혼식"))
                    .and(order.messageSchedule.eq("scheduled"))
                    .and(weddingDeadline.loe(nowStr))
                    .and(noSitePhoto);

        // 장례식·기타 지연
        com.querydsl.core.types.dsl.StringExpression nonWeddingDeadline =
                com.querydsl.core.types.dsl.Expressions.stringTemplate(
                    "CONCAT({0}, ' ', LPAD({1}, 2, '0'), ':', LPAD({2}, 2, '0'))",
                    order.deliveryDate, order.deliveryTimeHour, order.deliveryTimeMinute);

        com.querydsl.core.types.dsl.BooleanExpression isNonWeddingDelayed =
                order.status.eq(OrderStatus.ACCEPTED)
                    .and(order.eventType.ne("결혼식"))
                    .and(
                        order.deliveryTimeType.eq("3시간이내")
                            .and(order.acceptedAt.loe(threeHoursAgoStr))
                        .or(
                            order.deliveryTimeType.ne("3시간이내")
                                .and(order.deliveryTimeType.ne("직접입력"))
                                .and(nonWeddingDeadline.loe(nowStr))
                        )
                    );

        return new CaseBuilder()
                .when(order.status.eq(OrderStatus.PENDING)).then(1)
                .when(order.status.eq(OrderStatus.REJECTED)).then(2)
                .when(isWeddingDelayed.or(isNonWeddingDelayed)).then(3)
                .otherwise(4)
                .asc();
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
        if (Boolean.TRUE.equals(condition.deleted())) {
            where.and(order.isDeleted.isTrue());
        } else {
            where.and(order.isDeleted.isFalse());
        }
        if (condition.address() != null && !condition.address().isBlank()) {
            where.and(order.placeName.containsIgnoreCase(condition.address())
                    .or(order.address.containsIgnoreCase(condition.address()))
                    .or(order.roomName.containsIgnoreCase(condition.address())));
        }
        if (condition.recipientName() != null && !condition.recipientName().isBlank()) {
            where.and(order.recipientName.containsIgnoreCase(condition.recipientName()));
        }
        if (condition.vendorName() != null && !condition.vendorName().isBlank()) {
            QAdminJpaEntity admin = QAdminJpaEntity.adminJpaEntity;
            where.and(JPAExpressions.selectOne()
                    .from(admin)
                    .where(admin.id.eq(order.vendorId)
                            .and(admin.name.containsIgnoreCase(condition.vendorName())))
                    .exists());
        }
        if (condition.orderingCompanyName() != null && !condition.orderingCompanyName().isBlank()) {
            QCompanyJpaEntity company = QCompanyJpaEntity.companyJpaEntity;
            where.and(JPAExpressions.selectOne()
                    .from(company)
                    .where(company.id.eq(order.orderingCompanyId)
                            .and(company.companyName.containsIgnoreCase(condition.orderingCompanyName())))
                    .exists());
        }
        if (condition.vendorCompanyId() != null) {
            QAdminJpaEntity admin = QAdminJpaEntity.adminJpaEntity;
            where.and(order.vendorId.in(
                    JPAExpressions.select(admin.id)
                            .from(admin)
                            .where(admin.companyId.eq(condition.vendorCompanyId()))
            ));
        }
        if (condition.completedAtFrom() != null && !condition.completedAtFrom().isBlank()) {
            where.and(order.completedAt.goe(condition.completedAtFrom()));
        }
        if (condition.completedAtTo() != null && !condition.completedAtTo().isBlank()) {
            where.and(order.completedAt.loe(condition.completedAtTo() + " 23:59:59"));
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
