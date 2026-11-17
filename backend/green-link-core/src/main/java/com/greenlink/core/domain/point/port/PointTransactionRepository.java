package com.greenlink.core.domain.point.port;

import com.greenlink.core.domain.point.model.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction transaction);
    Page<PointTransaction> findByCompanyId(Long companyId, Pageable pageable);
}
