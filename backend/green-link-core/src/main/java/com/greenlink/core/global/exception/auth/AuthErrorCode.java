package com.greenlink.core.global.exception.auth;

import com.greenlink.core.global.exception.ErrorCode;

public enum AuthErrorCode implements ErrorCode {

    DUPLICATE_LOGIN_ID(400, "AUTH_001", "이미 사용 중인 아이디입니다."),
    DUPLICATE_BUSINESS_NO(400, "AUTH_002", "이미 등록된 사업자번호입니다."),
    COMPANY_NOT_FOUND(404, "AUTH_003", "등록된 업체를 찾을 수 없습니다."),
    INVALID_ROLE(400, "AUTH_004", "유효하지 않은 역할입니다.");

    private final int status;
    private final String code;
    private final String message;

    AuthErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
