package com.greenlink.admin.adapter.out.persistence.post;

import com.greenlink.core.domain.post.model.PostSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public PostQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Page<PostJpaEntity> search(PostSearchCondition condition, Pageable pageable) {
        QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        BooleanBuilder where = buildWhere(condition, post);

        NumberExpression<Integer> noticeFirst = new CaseBuilder()
                .when(post.isPinned.isTrue()
                        .and(post.noticeEndAt.isNull().or(post.noticeEndAt.after(now))))
                .then(0)
                .otherwise(1);

        List<PostJpaEntity> content = queryFactory
                .selectFrom(post)
                .where(where)
                .orderBy(noticeFirst.asc(), post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public List<PostJpaEntity> findActivePinnedNotices() {
        QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        return queryFactory
                .selectFrom(post)
                .where(
                        post.isPinned.isTrue(),
                        post.deletedAt.isNull(),
                        post.noticeEndAt.isNull().or(post.noticeEndAt.after(now))
                )
                .orderBy(post.createdAt.desc())
                .fetch();
    }

    private BooleanBuilder buildWhere(PostSearchCondition condition, QPostJpaEntity post) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(post.deletedAt.isNull());

        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            where.and(
                    post.title.containsIgnoreCase(condition.keyword())
                            .or(post.companyName.containsIgnoreCase(condition.keyword()))
            );
        }

        if (Boolean.TRUE.equals(condition.noticeOnly())) {
            where.and(post.isNotice.isTrue());
        }

        return where;
    }
}
