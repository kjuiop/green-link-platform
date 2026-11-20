package com.greenlink.core.application.sms.usecase;

import com.greenlink.core.application.sms.dto.BarobillQueryResult;

public interface BarobillQueryUseCase {

    /** companyId가 null이면 운영자 기본 계정 기준으로 발신번호 등록 여부를 조회한다. */
    BarobillQueryResult checkSenderNumber(Long companyId);

    BarobillQueryResult getSendState(Long companyId, String sendKey);
}
