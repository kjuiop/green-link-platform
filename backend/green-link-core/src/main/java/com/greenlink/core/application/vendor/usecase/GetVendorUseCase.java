package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.vendor.dto.VendorResult;

public interface GetVendorUseCase {
    VendorResult getVendor(Long id);
}
