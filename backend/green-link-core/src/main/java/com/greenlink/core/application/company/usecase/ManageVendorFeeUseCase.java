package com.greenlink.core.application.company.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.VendorFeeResult;
import org.springframework.data.domain.Pageable;

public interface ManageVendorFeeUseCase {
    PageResult<VendorFeeResult> getVendorFees(String keyword, Pageable pageable);
    VendorFeeResult updateVendorFee(Long companyId, long vendorFee);
}
