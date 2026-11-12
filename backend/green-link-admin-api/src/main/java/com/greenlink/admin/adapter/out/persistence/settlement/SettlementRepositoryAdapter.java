package com.greenlink.admin.adapter.out.persistence.settlement;

import com.greenlink.core.domain.settlement.model.Settlement;
import com.greenlink.core.domain.settlement.model.SettlementType;
import com.greenlink.core.domain.settlement.port.SettlementRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class SettlementRepositoryAdapter implements SettlementRepository {

    private final SettlementJpaRepository jpaRepository;

    public SettlementRepositoryAdapter(SettlementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Settlement> findByCompanyIdAndTypeAndPeriod(Long companyId, SettlementType type, String fromDate, String toDate) {
        return jpaRepository.findByCompanyIdAndTypeAndFromDateAndToDate(companyId, type, fromDate, toDate)
                .map(SettlementJpaEntity::toDomain);
    }

    @Override
    public List<Settlement> findAllByCompanyIdsAndTypeAndPeriod(List<Long> companyIds, SettlementType type, String fromDate, String toDate) {
        return jpaRepository.findAllByCompanyIdInAndTypeAndFromDateAndToDate(companyIds, type, fromDate, toDate)
                .stream().map(SettlementJpaEntity::toDomain).toList();
    }

    @Override
    public Settlement save(Settlement settlement) {
        return jpaRepository.save(SettlementJpaEntity.from(settlement)).toDomain();
    }

    @Override
    @Transactional
    public void deleteByCompanyIdAndTypeAndPeriod(Long companyId, SettlementType type, String fromDate, String toDate) {
        jpaRepository.deleteByCompanyIdAndTypeAndFromDateAndToDate(companyId, type, fromDate, toDate);
    }
}
