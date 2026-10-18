package com.greenlink.api.adapter.out.persistence.member;

import com.greenlink.core.domain.member.model.Member;
import com.greenlink.core.domain.member.port.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Component;

import static com.greenlink.api.adapter.out.persistence.member.QMemberJpaEntity.memberJpaEntity;

@Component
public class MemberRepositoryAdapter implements MemberRepository {

    private final MemberJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryAdapter(MemberJpaRepository jpaRepository,
                                    JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        Integer result = queryFactory
            .selectOne()
            .from(memberJpaEntity)
            .where(memberJpaEntity.loginId.eq(loginId))
            .fetchFirst();
        return result != null;
    }

    @Override
    public Member save(Member member) {
        return jpaRepository.save(MemberJpaEntity.from(member)).toDomain();
    }
}
