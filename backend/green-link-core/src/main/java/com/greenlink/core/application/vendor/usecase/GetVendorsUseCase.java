package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.vendor.dto.VendorResult;

import java.util.List;

public interface GetVendorsUseCase {
    List<VendorResult> getVendors(String status);
}
