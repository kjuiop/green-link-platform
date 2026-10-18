package com.greenlink.admin.adapter.out.persistence.company;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.greenlink.admin.adapter.out.persistence.company.QCompanyJpaEntity.companyJpaEntity;

@Component
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public CompanyRepositoryAdapter(CompanyJpaRepository jpaRepository,
                                     JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<Company> findByBusinessNo(String businessNo) {
        CompanyJpaEntity result = queryFactory
            .selectFrom(companyJpaEntity)
            .where(companyJpaEntity.businessNo.eq(businessNo))
            .fetchOne();
        return Optional.ofNullable(result).map(CompanyJpaEntity::toDomain);
    }

    @Override
    public Company save(Company company) {
        return jpaRepository.save(CompanyJpaEntity.from(company)).toDomain();
    }

    @Override
    public Optional<Company> findById(Long id) {
        return jpaRepository.findById(id).map(CompanyJpaEntity::toDomain);
    }

    @Override
    public boolean existsByBusinessNo(String businessNo) {
        return jpaRepository.existsByBusinessNo(businessNo);
    }
}
