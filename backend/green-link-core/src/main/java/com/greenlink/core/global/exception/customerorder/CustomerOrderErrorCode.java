package com.greenlink.core.global.exception.customerorder;

import com.greenlink.core.global.exception.ErrorCode;

public enum CustomerOrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(404, "CUSTOMER_ORDER_001", "고객 주문을 찾을 수 없습니다."),
    FORBIDDEN_COMPANY(403, "CUSTOMER_ORDER_002", "다른 발주사의 고객 주문에는 접근할 수 없습니다."),
    INVALID_STATE_TRANSITION(409, "CUSTOMER_ORDER_003", "현재 상태에서는 처리할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    CustomerOrderErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
