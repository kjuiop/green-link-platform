package com.greenlink.core.domain.order.model;

public enum OrderStatus {
    PENDING,    // 발주대기
    ACCEPTED,   // 수주완료
    DELIVERED,  // 배송완료
    REJECTED,   // 수주거절
    CANCELLED,  // 주문취소
    REFUND_REQUESTED, // 환불요청
    REFUNDED    // 환불완료
}
