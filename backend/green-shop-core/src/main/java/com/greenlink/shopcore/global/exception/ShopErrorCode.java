package com.greenlink.shopcore.global.exception;

public enum ShopErrorCode implements ErrorCode {

    AUTH_INVALID_CREDENTIALS(401, "SHOP_AUTH_001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    AUTH_ACCOUNT_INACTIVE(403, "SHOP_AUTH_002", "사용할 수 없는 계정입니다."),
    AUTH_UPSTREAM_UNAVAILABLE(503, "SHOP_AUTH_003", "계정 확인 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요."),
    ACCESS_ALREADY_REQUESTED(409, "SHOP_ACCESS_001", "이미 접근 신청했거나 승인된 계정입니다."),
    ACCESS_INVALID_TRANSITION(409, "SHOP_ACCESS_002", "처리할 수 없는 상태입니다."),
    ACCESS_REJECT_REASON_REQUIRED(400, "SHOP_ACCESS_003", "거절 사유를 입력해주세요."),
    ACCESS_NOT_REQUIRED(409, "SHOP_ACCESS_004", "접근 신청이 필요 없는 계정입니다."),
    ADMIN_REQUEST_NOT_FOUND(404, "SHOP_ACCESS_005", "신청 내역을 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    ShopErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
