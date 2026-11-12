package com.greenlink.core.application.vendor.usecase;

public interface ChangeVendorPasswordUseCase {
    void changePassword(Long vendorId, String newPassword);
}
