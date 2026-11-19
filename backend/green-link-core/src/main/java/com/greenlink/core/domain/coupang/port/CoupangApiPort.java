package com.greenlink.core.domain.coupang.port;

import com.greenlink.core.domain.coupang.model.CoupangCredentialSecret;
import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CoupangApiPort {

    /** from~to(KST) 구간의 신규(결제완료) 발주서를 페이징을 모두 처리해 반환한다. */
    List<CoupangOrderSheet> fetchNewOrderSheets(CoupangCredentialSecret secret, LocalDateTime from, LocalDateTime to);

    /** 쿠팡 주문번호의 발주서 중 해당 배송박스를 조회한다. 존재하지 않으면 empty. */
    Optional<CoupangOrderSheet> fetchOrderSheet(CoupangCredentialSecret secret, long orderId, long shipmentBoxId);
}
