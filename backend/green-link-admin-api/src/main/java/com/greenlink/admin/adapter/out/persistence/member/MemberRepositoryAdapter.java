package com.greenlink.admin.adapter.out.persistence.member;

import com.greenlink.core.domain.member.model.Member;
import com.greenlink.core.domain.member.port.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.greenlink.admin.adapter.out.persistence.member.QMemberJpaEntity.memberJpaEntity;

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

    @Override
    public Optional<Member> findById(Long id) {
        return jpaRepository.findById(id).map(MemberJpaEntity::toDomain);
    }

    @Override
    public List<Member> findAll() {
        return jpaRepository.findAll().stream()
            .map(MemberJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Member> findAllByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
            .map(MemberJpaEntity::toDomain)
            .toList();
    }
}
