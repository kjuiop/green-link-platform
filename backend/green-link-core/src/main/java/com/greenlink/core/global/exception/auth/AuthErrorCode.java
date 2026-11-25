package com.greenlink.core.global.exception.auth;

import com.greenlink.core.global.exception.ErrorCode;

public enum AuthErrorCode implements ErrorCode {

    DUPLICATE_LOGIN_ID(400, "AUTH_001", "이미 사용 중인 아이디입니다."),
    DUPLICATE_BUSINESS_NO(400, "AUTH_002", "이미 등록된 사업자번호입니다."),
    COMPANY_NOT_FOUND(404, "AUTH_003", "등록된 업체를 찾을 수 없습니다."),
    INVALID_ROLE(400, "AUTH_004", "유효하지 않은 역할입니다."),
    INVALID_CREDENTIALS(401, "AUTH_005", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_INACTIVE(403, "AUTH_006", "정지된 계정입니다."),
    ACCOUNT_PENDING(403, "AUTH_007", "승인 대기 중인 계정입니다. 관리자 승인 후 로그인하실 수 있습니다."),
    PHONE_CODE_INVALID(400, "AUTH_008", "인증번호가 올바르지 않거나 만료되었습니다."),
    PHONE_NOT_VERIFIED(400, "AUTH_009", "휴대폰 인증을 먼저 완료해주세요."),
    MEMBER_NOT_FOUND_BY_PHONE(404, "AUTH_010", "해당 연락처로 가입된 계정을 찾을 수 없습니다."),
    SMS_SEND_FAILED(500, "AUTH_011", "인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요."),
    INTERNAL_ACCESS_DENIED(403, "AUTH_012", "내부 호출 권한이 없습니다."),
    ONURIM_NOT_ENABLED(403, "AUTH_013", "온우림 이용 권한이 없습니다. 이용 신청 후 승인되면 사용할 수 있습니다."),
    COMPANY_ONURIM_NOT_ENABLED(403, "AUTH_014", "쇼핑몰 서비스로 가입된 회사입니다. 온우림 이용은 별도 신청이 필요합니다."),
    SHOP_LOGIN_ID_DUPLICATED(409, "AUTH_015", "이미 사용 중인 아이디입니다."),
    SHOP_BUSINESS_NO_REGISTERED(409, "AUTH_016", "이미 등록된 사업자번호입니다. 합류 신청으로 진행해주세요."),
    SHOP_SIGNUP_ROLE_INVALID(400, "AUTH_017", "신청할 수 없는 역할입니다."),
    INVALID_BUSINESS_NO(400, "AUTH_018", "사업자번호 형식이 올바르지 않습니다."),
    SHOP_COMPANY_NAME_REQUIRED(400, "AUTH_019", "회사명을 입력해주세요."),
    SHOP_ACCESS_COMPANY_MISMATCH(409, "AUTH_020", "이미 다른 회사에 소속된 계정입니다."),
    SHOP_ACCESS_COMPANY_NOT_FOUND(404, "AUTH_021", "회사를 찾을 수 없습니다."),
    SHOP_ACCESS_ADMIN_NOT_FOUND(404, "AUTH_022", "계정을 찾을 수 없습니다."),
    PASSWORD_NOT_SET(403, "AUTH_023", "비밀번호가 아직 설정되지 않은 계정입니다."),
    PASSWORD_SETUP_FAILED(400, "AUTH_024", "설정할 수 없습니다. 아이디·휴대폰 번호·설정 기간을 확인해주세요."),
    PASSWORD_SETUP_NOT_PENDING(409, "AUTH_025", "이미 비밀번호가 설정된 계정입니다.");

    private final int status;
    private final String code;
    private final String message;

    AuthErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
