package com.greenlink.shopcore.global.exception;

public class ShopBusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public ShopBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
