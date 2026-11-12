package com.greenlink.admin.adapter.out.persistence.settlement;

import com.greenlink.core.domain.settlement.model.SettlementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, Long> {

    Optional<SettlementJpaEntity> findByCompanyIdAndTypeAndFromDateAndToDate(
            Long companyId, SettlementType type, String fromDate, String toDate);

    List<SettlementJpaEntity> findAllByCompanyIdInAndTypeAndFromDateAndToDate(
            List<Long> companyIds, SettlementType type, String fromDate, String toDate);

    void deleteByCompanyIdAndTypeAndFromDateAndToDate(
            Long companyId, SettlementType type, String fromDate, String toDate);
}
