package com.greenlink.core.global.exception.product;

import com.greenlink.core.global.exception.ErrorCode;

public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(404, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    PRODUCT_BASE_COPY_READONLY(400, "PRODUCT_002", "기본 상품은 가격과 사용 여부만 변경할 수 있습니다."),
    BASE_PRODUCT_NOT_FOUND(404, "PRODUCT_003", "기본 상품을 찾을 수 없습니다."),
    PRODUCT_DISABLED(400, "PRODUCT_004", "발주할 수 없는 상품입니다."),
    PRODUCT_REQUIRED(400, "PRODUCT_005", "발주 상품을 선택해 주세요.");

    private final int status;
    private final String code;
    private final String message;

    ProductErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
