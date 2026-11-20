package com.greenlink.core.global.exception.taxinvoice;

import com.greenlink.core.global.exception.ErrorCode;

public enum TaxInvoiceErrorCode implements ErrorCode {

    TRANSACTION_NOT_FOUND(404, "TAX_001", "포인트 거래 내역을 찾을 수 없습니다."),
    NOT_CHARGE_TRANSACTION(400, "TAX_002", "충전 내역만 계산서를 발행할 수 있습니다."),
    ALREADY_ISSUED(409, "TAX_003", "이미 계산서가 발행된 충전 내역입니다."),
    ISSUE_IN_PROGRESS(409, "TAX_004", "계산서 발행이 진행 중입니다. 잠시 후 다시 확인해 주세요."),
    RECIPIENT_INFO_MISSING(400, "TAX_005", "발주사의 사업자번호·대표자명·주소가 모두 등록되어 있어야 합니다."),
    EMAIL_INVALID(400, "TAX_006", "수신 이메일 형식이 올바르지 않습니다."),
    SUPPLIER_NOT_CONFIGURED(400, "TAX_007", "바로빌 계정(연동인증키·아이디·사업자번호) 설정이 완료되지 않았습니다.");

    private final int status;
    private final String code;
    private final String message;

    TaxInvoiceErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
