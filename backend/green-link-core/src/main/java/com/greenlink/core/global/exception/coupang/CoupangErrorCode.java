package com.greenlink.core.global.exception.coupang;

import com.greenlink.core.global.exception.ErrorCode;

public enum CoupangErrorCode implements ErrorCode {

    CREDENTIAL_NOT_FOUND(404, "COUPANG_001", "쿠팡 연동 키가 등록되지 않았습니다."),
    COMPANY_REQUIRED(400, "COUPANG_002", "발주사를 선택해 주세요."),
    FORBIDDEN_COMPANY(403, "COUPANG_003", "다른 발주사의 쿠팡 정보에는 접근할 수 없습니다."),
    CIPHER_NOT_CONFIGURED(500, "COUPANG_004", "쿠팡 연동 암호화 키가 서버에 설정되지 않았습니다."),
    KEY_EXPIRED(400, "COUPANG_005", "쿠팡 Open API 키가 만료되었습니다. WING에서 재발급 후 다시 등록해 주세요."),
    ALREADY_COLLECTING(409, "COUPANG_006", "이미 쿠팡 주문을 불러오는 중입니다."),
    ORDER_NOT_FOUND(404, "COUPANG_007", "쿠팡 주문을 찾을 수 없습니다."),
    NOT_APPROVABLE(409, "COUPANG_008", "승인할 수 없는 상태의 쿠팡 주문입니다."),
    CANCELED_ON_COUPANG(409, "COUPANG_009", "쿠팡에서 취소된 주문입니다."),
    VERIFY_FAILED(502, "COUPANG_010", "쿠팡 주문 상태를 확인하지 못했습니다. 잠시 후 다시 시도해 주세요."),
    INVALID_CREDENTIAL(400, "COUPANG_011", "업체코드, Access Key, Secret Key, 키 발급일을 모두 입력해 주세요."),
    INVALID_STATE_TRANSITION(409, "COUPANG_012", "현재 상태에서는 처리할 수 없습니다."),
    COLLECT_FAILED(502, "COUPANG_013", "쿠팡 주문을 불러오지 못했습니다. 연동 설정 화면의 오류 내용을 확인해 주세요.");

    private final int status;
    private final String code;
    private final String message;

    CoupangErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
