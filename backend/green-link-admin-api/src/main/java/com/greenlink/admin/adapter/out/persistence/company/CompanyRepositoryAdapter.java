package com.greenlink.admin.adapter.out.persistence.company;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyCapability;
import com.greenlink.core.domain.company.model.CompanyRegion;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.greenlink.admin.adapter.out.persistence.company.QCompanyJpaEntity.companyJpaEntity;

@Component
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;
    private final CompanyRegionJpaRepository regionJpaRepository;
    private final CompanyCapabilityJpaRepository capabilityJpaRepository;
    private final JPAQueryFactory queryFactory;

    public CompanyRepositoryAdapter(CompanyJpaRepository jpaRepository,
                                     CompanyRegionJpaRepository regionJpaRepository,
                                     CompanyCapabilityJpaRepository capabilityJpaRepository,
                                     JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.regionJpaRepository = regionJpaRepository;
        this.capabilityJpaRepository = capabilityJpaRepository;
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

    @Override
    public List<Company> findAllByIdIn(List<Long> ids) {
        return jpaRepository.findByIdIn(ids).stream()
            .map(CompanyJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Company> findAllByType(CompanyType type) {
        return jpaRepository.findByTypeAndDisplayYnTrue(type).stream()
            .map(CompanyJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Company> findAllByTypeIn(List<CompanyType> types) {
        return jpaRepository.findByTypeInAndDisplayYnTrue(types).stream()
            .map(CompanyJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Company> findByCompanyName(String companyName) {
        return jpaRepository.findByCompanyName(companyName).map(CompanyJpaEntity::toDomain);
    }

    @Override
    public Page<Company> findAllDisplayed(Pageable pageable) {
        return jpaRepository.findAllByDisplayYnTrue(pageable)
            .map(CompanyJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveRegions(Long companyId, List<CompanyRegion> regions) {
        regionJpaRepository.deleteAllByCompanyId(companyId);
        regionJpaRepository.saveAll(regions.stream()
            .map(CompanyRegionJpaEntity::from)
            .toList());
    }

    @Override
    @Transactional
    public void saveCapabilities(Long companyId, List<CompanyCapability> capabilities) {
        capabilityJpaRepository.deleteAllByCompanyId(companyId);
        capabilityJpaRepository.saveAll(capabilities.stream()
            .map(c -> CompanyCapabilityJpaEntity.from(companyId, c))
            .toList());
    }

    @Override
    public List<CompanyRegion> findRegionsByCompanyId(Long companyId) {
        return regionJpaRepository.findAllByCompanyId(companyId).stream()
            .map(CompanyRegionJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<CompanyCapability> findCapabilitiesByCompanyId(Long companyId) {
        return capabilityJpaRepository.findAllByCompanyId(companyId).stream()
            .map(CompanyCapabilityJpaEntity::toDomain)
            .toList();
    }
}
