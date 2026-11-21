package com.greenlink.core.global.exception.company;

import com.greenlink.core.global.exception.ErrorCode;

public enum CompanyErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND(404, "COMPANY_001", "업체를 찾을 수 없습니다."),
    ORDERING_COMPANY_NOT_FOUND(404, "COMPANY_002", "발주사를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    CompanyErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
