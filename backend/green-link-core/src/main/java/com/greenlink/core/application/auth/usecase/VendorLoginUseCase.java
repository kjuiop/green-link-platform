package com.greenlink.core.application.auth.usecase;

import com.greenlink.core.application.auth.dto.VendorLoginCommand;
import com.greenlink.core.application.auth.dto.VendorLoginResult;

public interface VendorLoginUseCase {
    VendorLoginResult login(VendorLoginCommand cmd);
}
