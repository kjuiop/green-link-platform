package com.greenlink.core.global.exception.fax;

import com.greenlink.core.global.exception.ErrorCode;

public enum FaxErrorCode implements ErrorCode {

    FAX_MESSAGE_NOT_FOUND(404, "FAX_001", "팩스 발송 내역을 찾을 수 없습니다."),
    FAX_NOT_ALLOWED(400, "FAX_002", "수주완료 상태의 주문만 팩스를 발송할 수 있습니다."),
    FAX_VENDOR_NOT_ENABLED(400, "FAX_003", "팩스가 연동되지 않았거나 팩스번호가 없는 수주사입니다."),
    FAX_NOT_CONFIGURED(400, "FAX_004", "팩스 연동 설정(바로빌 계정·팩스 발신번호·FTP)이 완료되지 않았습니다."),
    FAX_NUMBER_REQUIRED(400, "FAX_005", "팩스 연동 시 팩스번호를 입력해야 합니다."),
    FAX_URL_INVALID(400, "FAX_006", "팩스 API URL은 바로빌(baroservice.com)의 https 주소여야 합니다."),
    FAX_FTP_PORT_INVALID(400, "FAX_007", "FTP 포트는 1~65535 범위여야 합니다.");

    private final int status;
    private final String code;
    private final String message;

    FaxErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
