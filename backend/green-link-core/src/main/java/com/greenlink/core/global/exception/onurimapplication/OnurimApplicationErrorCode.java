package com.greenlink.core.global.exception.onurimapplication;

import com.greenlink.core.global.exception.ErrorCode;

public enum OnurimApplicationErrorCode implements ErrorCode {

    NOT_FOUND(404, "ONURIM_APP_001", "온우림 이용 신청을 찾을 수 없습니다."),
    ALREADY_ENABLED(409, "ONURIM_APP_002", "이미 온우림을 이용 중인 회사입니다."),
    ALREADY_PENDING(409, "ONURIM_APP_003", "심사 중인 신청이 이미 있습니다."),
    NOT_PENDING(409, "ONURIM_APP_004", "이미 처리된 신청입니다."),
    INVALID_APPLICATION(400, "ONURIM_APP_005", "희망 유형, 연락처, 주소를 입력해주세요."),
    TYPE_REQUIRED(400, "ONURIM_APP_006", "승인할 이용 유형을 선택해주세요."),
    REJECT_REASON_REQUIRED(400, "ONURIM_APP_007", "거절 사유를 입력해주세요. (500자 이내)"),
    APPLICANT_MISMATCH(403, "ONURIM_APP_008", "해당 회사 소속 계정만 신청할 수 있습니다."),
    DOCUMENT_REQUIRED(400, "ONURIM_APP_009", "사업자등록증 사본과 통장 사본을 첨부해주세요."),
    DOCUMENT_NOT_ALLOWED(400, "ONURIM_APP_010", "이미 등록된 서류는 다시 첨부할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    OnurimApplicationErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
