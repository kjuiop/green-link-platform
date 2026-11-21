package com.greenlink.admin.adapter.out.persistence.customerorder;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CustomerOrderQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public PageResult<CustomerOrder> findByCompany(Long companyId, OrderChannel channel,
                                                   CustomerOrderStatus status, int page, int size) {
        QCustomerOrderJpaEntity order = QCustomerOrderJpaEntity.customerOrderJpaEntity;
        BooleanBuilder where = buildWhere(order, companyId, channel, status);

        QCoupangOrderDetailJpaEntity detail = QCoupangOrderDetailJpaEntity.coupangOrderDetailJpaEntity;
        List<CustomerOrderJpaEntity> content = queryFactory
                .selectFrom(order)
                .leftJoin(order.coupangDetail, detail).fetchJoin()
                .where(where)
                .orderBy(order.orderedAt.desc(), order.id.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(where)
                .fetchOne();

        Page<CustomerOrder> resultPage = new PageImpl<>(
                content.stream().map(CustomerOrderJpaEntity::toDomain).toList(),
                PageRequest.of(page, size),
                total != null ? total : 0L
        );
        return PageResult.of(resultPage);
    }

    public long countPending(Long companyId, OrderChannel channel) {
        QCustomerOrderJpaEntity order = QCustomerOrderJpaEntity.customerOrderJpaEntity;
        BooleanBuilder where = buildWhere(order, companyId, channel, CustomerOrderStatus.PENDING);
        Long count = queryFactory
                .select(order.count())
                .from(order)
                .where(where)
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanBuilder buildWhere(QCustomerOrderJpaEntity order, Long companyId,
                                      OrderChannel channel, CustomerOrderStatus status) {
        BooleanBuilder where = new BooleanBuilder();
        if (companyId != null) {
            where.and(order.companyId.eq(companyId));
        }
        if (channel != null) {
            where.and(order.channel.eq(channel));
        }
        if (status != null) {
            where.and(order.status.eq(status));
        }
        return where;
    }
}
