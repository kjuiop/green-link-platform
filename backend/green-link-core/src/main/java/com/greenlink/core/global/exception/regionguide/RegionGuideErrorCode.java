package com.greenlink.core.global.exception.regionguide;

import com.greenlink.core.global.exception.ErrorCode;

public enum RegionGuideErrorCode implements ErrorCode {

    REGION_GUIDE_NOT_FOUND(404, "REGION_GUIDE_001", "지역 안내문구를 찾을 수 없습니다."),
    REGION_GUIDE_INVALID_SIDO(400, "REGION_GUIDE_002", "올바른 시·도 명칭이 아닙니다."),
    REGION_GUIDE_INVALID_INPUT(400, "REGION_GUIDE_003", "장소명과 조건 문구를 확인해 주세요.");

    private final int status;
    private final String code;
    private final String message;

    RegionGuideErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
