package com.greenlink.core.global.exception.vendor;

import com.greenlink.core.global.exception.ErrorCode;

public enum VendorErrorCode implements ErrorCode {

    VENDOR_NOT_FOUND(404, "VENDOR_001", "수주업체를 찾을 수 없습니다."),
    DUPLICATE_LOGIN_ID(400, "VENDOR_002", "이미 사용 중인 아이디입니다."),
    DUPLICATE_BUSINESS_NO(400, "VENDOR_003", "이미 등록된 사업자번호입니다.");

    private final int status;
    private final String code;
    private final String message;

    VendorErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
