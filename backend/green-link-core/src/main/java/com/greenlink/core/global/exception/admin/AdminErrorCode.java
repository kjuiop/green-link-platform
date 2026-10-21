package com.greenlink.core.global.exception.admin;

import com.greenlink.core.global.exception.ErrorCode;

public enum AdminErrorCode implements ErrorCode {

    ADMIN_NOT_FOUND(404, "ADMIN_001", "관리자 정보를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    AdminErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
