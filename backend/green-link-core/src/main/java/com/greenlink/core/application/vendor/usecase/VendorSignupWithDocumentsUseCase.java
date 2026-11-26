package com.greenlink.core.application.vendor.usecase;

import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.application.vendor.dto.VendorSignupWithDocumentsResult;

public interface VendorSignupWithDocumentsUseCase {
    VendorSignupWithDocumentsResult signup(VendorSignupCommand cmd, SignupDocument businessReg, SignupDocument bankBook);
}
