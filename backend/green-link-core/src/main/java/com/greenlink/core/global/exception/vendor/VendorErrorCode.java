package com.greenlink.core.global.exception.vendor;

import com.greenlink.core.global.exception.ErrorCode;

public enum VendorErrorCode implements ErrorCode {

    VENDOR_NOT_FOUND(404, "VENDOR_001", "수주업체를 찾을 수 없습니다."),
    DUPLICATE_LOGIN_ID(400, "VENDOR_002", "이미 사용 중인 아이디입니다."),
    DUPLICATE_BUSINESS_NO(400, "VENDOR_003", "이미 등록된 사업자번호입니다."),
    VENDOR_ALREADY_DELETED(400, "VENDOR_004", "이미 삭제된 업체입니다."),
    DOCUMENT_REQUIRED(400, "VENDOR_005", "사업자 사본과 통장 사본을 모두 첨부해주세요."),
    DOCUMENT_INVALID_TYPE(400, "VENDOR_006", "이미지(JPG, PNG, WEBP, GIF, HEIC) 또는 PDF 파일만 첨부할 수 있습니다."),
    DOCUMENT_TOO_LARGE(400, "VENDOR_007", "서류 파일은 10MB 이하만 첨부할 수 있습니다.");

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
