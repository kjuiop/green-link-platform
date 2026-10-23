package com.greenlink.admin.adapter.out.persistence.admin;

import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public Optional<Admin> findById(Long id) {
        return jpaRepository.findById(id).map(AdminJpaEntity::toDomain);
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

    @Override
    public List<Admin> findAllVendors() {
        return queryFactory
            .selectFrom(adminJpaEntity)
            .where(adminJpaEntity.companyId.isNotNull())
            .orderBy(adminJpaEntity.createdAt.desc())
            .fetch()
            .stream()
            .map(AdminJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Admin> findAllByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
            .map(AdminJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Page<Admin> findVendorsPaged(String status, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(adminJpaEntity.companyId.isNotNull());
        if (status != null && !status.isBlank()) {
            AdminStatus adminStatus = AdminStatus.valueOf(status.toUpperCase());
            where.and(adminJpaEntity.status.eq(adminStatus));
        }

        List<AdminJpaEntity> content = queryFactory
                .selectFrom(adminJpaEntity)
                .where(where)
                .orderBy(adminJpaEntity.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(adminJpaEntity.count())
                .from(adminJpaEntity)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L)
                .map(AdminJpaEntity::toDomain);
    }
}
