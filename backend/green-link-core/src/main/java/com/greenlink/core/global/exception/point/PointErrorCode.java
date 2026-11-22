package com.greenlink.core.global.exception.point;

import com.greenlink.core.global.exception.ErrorCode;

public enum PointErrorCode implements ErrorCode {

    INSUFFICIENT_POINT(400, "POINT_001", "포인트가 부족합니다. 충전 후 다시 시도해주세요.");

    private final int status;
    private final String code;
    private final String message;

    PointErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
