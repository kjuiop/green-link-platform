package com.greenlink.core.global.exception.order;

import com.greenlink.core.global.exception.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(404, "ORDER_001", "발주를 찾을 수 없습니다."),
    INVALID_STATUS_TRANSITION(400, "ORDER_002", "허용되지 않는 상태 변경입니다."),
    INVALID_ORDER_STATUS(400, "ORDER_003", "유효하지 않은 주문 상태 값입니다."),
    INVALID_ORDER_ACTION(400, "ORDER_004", "유효하지 않은 주문 상태 변경 요청입니다."),
    SITE_PHOTO_REQUIRED(400, "ORDER_005", "배송사진을 1장 이상 업로드해야 배송완료 처리가 가능합니다."),
    SETTLED_ORDER_AMOUNT_LOCKED(400, "ORDER_006", "정산이 완료된 주문은 금액을 수정할 수 없습니다."),
    REFUND_ORDER_LOCKED(400, "ORDER_007", "환불요청 또는 환불완료 상태의 주문은 수정할 수 없습니다."),
    REFUND_ORDER_NOT_DELETABLE(400, "ORDER_008", "환불요청 또는 환불완료 상태의 주문은 삭제할 수 없습니다."),
    ADJUST_ONLY_DELIVERED(400, "ORDER_009", "배송완료 상태의 주문만 발주금액을 조정할 수 있습니다."),
    ADJUST_PENDING_EXISTS(400, "ORDER_010", "이미 승인 대기 중인 금액 수정 요청이 있습니다."),
    ADJUST_NOT_FOUND(404, "ORDER_011", "금액 수정 요청을 찾을 수 없습니다."),
    ADJUST_NOT_PENDING(400, "ORDER_012", "이미 처리된 금액 수정 요청입니다."),
    ADJUST_TOTAL_CHANGED(400, "ORDER_013", "요청 이후 발주금액이 변경되어 승인할 수 없습니다. 새로 요청해주세요."),
    ADJUST_INVALID_AMOUNT(400, "ORDER_014", "조정 후 발주금액은 0원 이상이며 현재 금액과 달라야 합니다."),
    ADJUST_SETTLED_LOCKED(400, "ORDER_015", "정산이 완료된 기간의 주문은 금액을 조정할 수 없습니다."),
    ADJUST_FORBIDDEN(403, "ORDER_016", "금액 수정 요청 권한이 없습니다."),
    ADJUST_REASON_REQUIRED(400, "ORDER_017", "사유를 입력해주세요."),
    ORDER_HAS_PENDING_ADJUST(400, "ORDER_018", "승인 대기 중인 금액 수정 요청이 있는 주문은 금액을 수정하거나 삭제할 수 없습니다."),
    DELIVERED_AMOUNT_LOCKED(400, "ORDER_019", "배송완료된 주문의 금액은 직접 수정할 수 없습니다. 금액 수정 요청을 이용해주세요.");

    private final int status;
    private final String code;
    private final String message;

    OrderErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
