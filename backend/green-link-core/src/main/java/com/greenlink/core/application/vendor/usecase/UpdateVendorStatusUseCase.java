package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.vendor.dto.VendorResult;

public interface UpdateVendorStatusUseCase {
    VendorResult updateVendorStatus(Long id, String action);
}
