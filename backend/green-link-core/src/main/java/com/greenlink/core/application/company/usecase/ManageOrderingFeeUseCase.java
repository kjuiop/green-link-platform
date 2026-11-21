package com.greenlink.core.application.company.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.OrderingFeeResult;
import com.greenlink.core.domain.company.model.OrderingFeeDiscountType;
import org.springframework.data.domain.Pageable;

public interface ManageOrderingFeeUseCase {
    PageResult<OrderingFeeResult> getOrderingFees(String keyword, Pageable pageable);
    OrderingFeeResult updateOrderingFee(Long companyId, OrderingFeeDiscountType discountType, long discountValue);
}
