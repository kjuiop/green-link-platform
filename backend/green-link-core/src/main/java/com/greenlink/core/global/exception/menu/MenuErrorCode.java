package com.greenlink.core.global.exception.menu;

import com.greenlink.core.global.exception.ErrorCode;

public enum MenuErrorCode implements ErrorCode {

    MENU_NOT_FOUND(404, "MENU_001", "메뉴를 찾을 수 없습니다."),
    MENU_HAS_CHILDREN(400, "MENU_002", "하위 메뉴가 있어 삭제할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    MenuErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
