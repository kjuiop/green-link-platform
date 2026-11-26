package com.greenlink.shopcore.global.exception;

public enum ShopErrorCode implements ErrorCode {

    AUTH_INVALID_CREDENTIALS(401, "SHOP_AUTH_001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    AUTH_ACCOUNT_INACTIVE(403, "SHOP_AUTH_002", "사용할 수 없는 계정입니다."),
    AUTH_UPSTREAM_UNAVAILABLE(503, "SHOP_AUTH_003", "계정 확인 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요."),
    AUTH_PASSWORD_NOT_SET(403, "SHOP_AUTH_004", "비밀번호가 아직 설정되지 않은 계정입니다. 비밀번호를 설정해주세요."),
    AUTH_PASSWORD_SETUP_FAILED(400, "SHOP_AUTH_005", "설정할 수 없습니다. 아이디·휴대폰 번호·설정 기간을 확인해주세요."),
    AUTH_PASSWORD_SETUP_NOT_PENDING(409, "SHOP_AUTH_006", "이미 비밀번호가 설정된 계정입니다."),
    ACCESS_ALREADY_REQUESTED(409, "SHOP_ACCESS_001", "이미 접근 신청했거나 승인된 계정입니다."),
    ACCESS_INVALID_TRANSITION(409, "SHOP_ACCESS_002", "처리할 수 없는 상태입니다."),
    ACCESS_REJECT_REASON_REQUIRED(400, "SHOP_ACCESS_003", "거절 사유를 입력해주세요."),
    ACCESS_NOT_REQUIRED(409, "SHOP_ACCESS_004", "접근 신청이 필요 없는 계정입니다."),
    ADMIN_REQUEST_NOT_FOUND(404, "SHOP_ACCESS_005", "신청 내역을 찾을 수 없습니다."),
    ACCESS_COMPANY_REQUIRED(409, "SHOP_ACCESS_006", "소속 회사가 있어야 접근 신청을 할 수 있습니다."),
    ACCESS_ROLE_NOT_ASSIGNABLE(400, "SHOP_ACCESS_007", "신청하거나 부여할 수 없는 역할입니다."),
    ACCESS_FIRST_MEMBER_MUST_BE_PARTNER(400, "SHOP_ACCESS_008", "회사의 첫 승인 멤버는 파트너 관리자여야 합니다."),
    ACCESS_ROLE_EXCEEDS_REQUEST(400, "SHOP_ACCESS_009", "신청한 역할보다 높은 역할로 승인할 수 없습니다."),
    ACCESS_COMPANY_MISMATCH(409, "SHOP_ACCESS_010", "이미 다른 회사에 소속된 계정입니다."),
    BRAND_NOT_FOUND(404, "SHOP_BRAND_001", "브랜드를 찾을 수 없습니다."),
    ACCESS_MANAGE_FORBIDDEN(403, "SHOP_ACCESS_012", "해당 구성원을 관리할 권한이 없습니다."),
    MEMBER_NOT_APPROVED(409, "SHOP_MEMBER_001", "승인된 구성원만 처리할 수 있습니다."),
    MEMBER_BRAND_REQUIRED(400, "SHOP_MEMBER_002", "브랜드 관리자는 브랜드를 1개 이상 지정해야 합니다."),
    MEMBER_BRAND_INVALID(400, "SHOP_MEMBER_003", "지정할 수 없는 브랜드입니다."),
    MEMBER_LAST_PARTNER(409, "SHOP_MEMBER_004", "회사의 마지막 파트너 관리자는 삭제하거나 강등할 수 없습니다."),
    BRAND_ACCESS_DENIED(403, "SHOP_BRAND_002", "해당 브랜드에 접근할 권한이 없습니다."),
    BRAND_NAME_INVALID(400, "SHOP_BRAND_003", "브랜드 이름은 1~100자로 입력해주세요."),
    BRAND_NAME_DUPLICATED(409, "SHOP_BRAND_004", "이미 같은 이름의 브랜드가 있습니다."),
    BRAND_COMPANY_REQUIRED(400, "SHOP_BRAND_005", "브랜드를 만들 회사를 지정해주세요."),
    SIGNUP_RATE_LIMITED(429, "SHOP_SIGNUP_001", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    SIGNUP_LOGIN_ID_DUPLICATED(409, "SHOP_SIGNUP_002", "이미 사용 중인 아이디입니다."),
    SIGNUP_BUSINESS_NO_REGISTERED(409, "SHOP_SIGNUP_003", "이미 등록된 사업자번호입니다."),
    SIGNUP_INVALID_INPUT(400, "SHOP_SIGNUP_004", "입력값을 확인해주세요."),
    MEMBER_COMPANY_REQUIRED(400, "SHOP_MEMBER_005", "계정을 만들 회사를 지정해주세요."),
    ONURIM_ALREADY_ENABLED(409, "SHOP_ONURIM_001", "이미 온우림을 이용 중인 회사입니다."),
    ONURIM_APPLICATION_ALREADY_PENDING(409, "SHOP_ONURIM_002", "심사 중인 신청이 이미 있습니다."),
    ONURIM_APPLICATION_INVALID(400, "SHOP_ONURIM_003", "신청 내용을 확인해주세요. (이용 유형, 연락처, 주소는 필수입니다.)");

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
