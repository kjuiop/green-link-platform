package com.greenlink.core.global.exception.photo;

import com.greenlink.core.global.exception.ErrorCode;

public enum PhotoErrorCode implements ErrorCode {

    PHOTO_NOT_FOUND(404, "PHOTO_001", "사진을 찾을 수 없습니다."),
    PHOTO_UPLOAD_FAILED(500, "PHOTO_002", "사진 업로드에 실패했습니다."),
    INVALID_PHOTO_TYPE(400, "PHOTO_003", "유효하지 않은 사진 유형입니다.");

    private final int status;
    private final String code;
    private final String message;

    PhotoErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
