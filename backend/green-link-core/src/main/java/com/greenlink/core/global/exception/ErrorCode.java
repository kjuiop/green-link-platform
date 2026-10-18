package com.greenlink.core.global.exception;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}
