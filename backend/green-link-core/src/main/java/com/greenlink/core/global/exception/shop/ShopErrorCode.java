package com.greenlink.core.global.exception.shop;

import com.greenlink.core.global.exception.ErrorCode;

public enum ShopErrorCode implements ErrorCode {

    INVALID_SLUG(400, "SHOP_001", "주소는 영문 소문자·숫자·하이픈으로 3~30자여야 하며 사용할 수 없는 단어가 포함되어 있습니다."),
    SLUG_TAKEN(409, "SHOP_002", "이미 사용 중인 주소입니다."),
    SHOP_NOT_FOUND(404, "SHOP_003", "쇼핑몰을 찾을 수 없습니다."),
    SHOP_ALREADY_EXISTS(409, "SHOP_004", "이미 쇼핑몰이 만들어져 있습니다."),
    PRODUCT_NOT_FOUND(404, "SHOP_005", "상품을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(404, "SHOP_006", "쇼핑몰 주문을 찾을 수 없습니다."),
    NOT_APPROVABLE(409, "SHOP_007", "승인할 수 없는 상태의 쇼핑몰 주문입니다."),
    INVALID_STATE_TRANSITION(409, "SHOP_008", "현재 상태에서는 처리할 수 없습니다."),
    FORBIDDEN_COMPANY(403, "SHOP_009", "다른 회사의 쇼핑몰 정보에는 접근할 수 없습니다."),
    TOO_MANY_REQUESTS(429, "SHOP_010", "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    INVALID_ORDER_REQUEST(400, "SHOP_011", "주문 정보를 확인해 주세요."),
    COMPANY_REQUIRED(400, "SHOP_012", "대상 회사를 선택해 주세요."),
    INVALID_SETTINGS(400, "SHOP_013", "쇼핑몰 설정 값을 확인해 주세요."),
    NOT_PUBLISHABLE(400, "SHOP_014", "공개하려면 표시 이름, 대표 전화, 노출 상품 1개 이상이 필요합니다."),
    INVALID_IMAGE(400, "SHOP_015", "이미지 파일(jpg, png, webp, 5MB 이하)만 올릴 수 있습니다."),
    ORDER_LOOKUP_FAILED(404, "SHOP_016", "주문번호 또는 전화번호가 일치하는 주문이 없습니다."),
    INVALID_VENDOR(400, "SHOP_017", "수주사는 본인 회사의 계정으로만 발주할 수 있습니다.");

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
