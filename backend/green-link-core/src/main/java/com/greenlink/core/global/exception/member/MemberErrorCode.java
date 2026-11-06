package com.greenlink.core.global.exception.member;

import com.greenlink.core.global.exception.ErrorCode;

public enum MemberErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND(404, "MEMBER_001", "업체 정보를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    MemberErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
