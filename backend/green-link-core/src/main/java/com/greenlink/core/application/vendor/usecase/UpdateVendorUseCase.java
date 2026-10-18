package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.vendor.dto.UpdateVendorCommand;
import com.greenlink.core.application.vendor.dto.VendorResult;

public interface UpdateVendorUseCase {
    VendorResult updateVendor(Long id, UpdateVendorCommand cmd);
}
