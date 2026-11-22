package com.greenlink.core.global.exception.sms;

import com.greenlink.core.global.exception.ErrorCode;

public enum SmsErrorCode implements ErrorCode {

    BAROBILL_NOT_CONFIGURED(400, "SMS_001", "바로빌 계정 정보가 설정되지 않았습니다."),
    BAROBILL_UNAVAILABLE(502, "SMS_002", "바로빌 서버와 통신할 수 없습니다."),
    BAROBILL_FORBIDDEN_COMPANY(403, "SMS_003", "해당 업체의 바로빌 정보를 조회할 권한이 없습니다."),
    BAROBILL_BUSINESS_NO_MISSING(400, "SMS_004", "회사 정보에 사업자번호가 없습니다. 회사 정보에 사업자번호를 먼저 등록하세요.");

    private final int status;
    private final String code;
    private final String message;

    SmsErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
