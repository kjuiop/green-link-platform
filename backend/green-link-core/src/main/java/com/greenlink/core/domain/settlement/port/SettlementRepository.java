package com.greenlink.core.domain.settlement.port;

import com.greenlink.core.domain.settlement.model.Settlement;
import com.greenlink.core.domain.settlement.model.SettlementType;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository {
    Optional<Settlement> findByCompanyIdAndTypeAndPeriod(Long companyId, SettlementType type, String fromDate, String toDate);
    List<Settlement> findAllByCompanyIdsAndTypeAndPeriod(List<Long> companyIds, SettlementType type, String fromDate, String toDate);
    Settlement save(Settlement settlement);
    void deleteByCompanyIdAndTypeAndPeriod(Long companyId, SettlementType type, String fromDate, String toDate);
}
