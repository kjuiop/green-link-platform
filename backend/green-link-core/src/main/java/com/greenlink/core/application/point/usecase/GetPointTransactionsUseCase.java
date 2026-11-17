package com.greenlink.core.application.point.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.point.dto.PointTransactionResult;
import org.springframework.data.domain.Pageable;

public interface GetPointTransactionsUseCase {
    PageResult<PointTransactionResult> getTransactions(Long companyId, Pageable pageable);
}
