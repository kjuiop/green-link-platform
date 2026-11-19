package com.greenlink.admin.adapter.out.persistence.coupang;

import com.greenlink.core.domain.coupang.model.CoupangCredential;
import com.greenlink.core.domain.coupang.port.CoupangCredentialRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CoupangCredentialRepositoryAdapter implements CoupangCredentialRepository {

    private final CoupangCredentialJpaRepository jpaRepository;

    public CoupangCredentialRepositoryAdapter(CoupangCredentialJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CoupangCredential> findByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyId(companyId).map(CoupangCredentialJpaEntity::toDomain);
    }

    @Override
    public List<Long> findEnabledCompanyIds() {
        return jpaRepository.findEnabledCompanyIds();
    }

    @Override
    public CoupangCredential save(CoupangCredential credential) {
        CoupangCredentialJpaEntity entity = jpaRepository.findByCompanyId(credential.getCompanyId())
                .orElseGet(() -> CoupangCredentialJpaEntity.from(credential));
        entity.apply(credential);
        return jpaRepository.save(entity).toDomain();
    }
}
