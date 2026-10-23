package com.greenlink.core.global.exception.order;

import com.greenlink.core.global.exception.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(404, "ORDER_001", "발주를 찾을 수 없습니다."),
    INVALID_STATUS_TRANSITION(400, "ORDER_002", "허용되지 않는 상태 변경입니다."),
    INVALID_ORDER_STATUS(400, "ORDER_003", "유효하지 않은 주문 상태 값입니다."),
    INVALID_ORDER_ACTION(400, "ORDER_004", "유효하지 않은 주문 상태 변경 요청입니다."),
    SITE_PHOTO_REQUIRED(400, "ORDER_005", "배송사진을 1장 이상 업로드해야 배송완료 처리가 가능합니다.");

    private final int status;
    private final String code;
    private final String message;

    OrderErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
