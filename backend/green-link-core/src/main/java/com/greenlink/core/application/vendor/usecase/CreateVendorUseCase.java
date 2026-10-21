package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import com.greenlink.core.application.vendor.dto.VendorResult;

public interface CreateVendorUseCase {
    VendorResult createVendor(CreateVendorCommand cmd);
}
