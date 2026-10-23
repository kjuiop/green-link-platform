package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.vendor.dto.VendorResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetVendorsUseCase {
    List<VendorResult> getVendors(String status);
    PageResult<VendorResult> getVendorsPaged(String status, Pageable pageable);
}
