package com.greenlink.admin.adapter.out.persistence.admin;

import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.greenlink.admin.adapter.out.persistence.admin.QAdminJpaEntity.adminJpaEntity;

@Component
public class AdminRepositoryAdapter implements AdminRepository {

    private final AdminJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public AdminRepositoryAdapter(AdminJpaRepository jpaRepository,
                                   JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<Admin> findByLoginId(String loginId) {
        AdminJpaEntity result = queryFactory
            .selectFrom(adminJpaEntity)
            .where(adminJpaEntity.loginId.eq(loginId))
            .fetchOne();
        return Optional.ofNullable(result).map(AdminJpaEntity::toDomain);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        Integer result = queryFactory
            .selectOne()
            .from(adminJpaEntity)
            .where(adminJpaEntity.loginId.eq(loginId))
            .fetchFirst();
        return result != null;
    }

    @Override
    public Admin save(Admin admin) {
        return jpaRepository.save(AdminJpaEntity.from(admin)).toDomain();
    }
}
