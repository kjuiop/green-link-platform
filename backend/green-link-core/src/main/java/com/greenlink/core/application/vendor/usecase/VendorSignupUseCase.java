package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.application.vendor.dto.VendorResult;

public interface VendorSignupUseCase {
    VendorResult signup(VendorSignupCommand cmd);
}
