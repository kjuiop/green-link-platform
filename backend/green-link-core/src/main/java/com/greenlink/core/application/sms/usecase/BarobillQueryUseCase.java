package com.greenlink.core.application.sms.usecase;

import com.greenlink.core.application.sms.dto.BarobillKakaoChannelsResult;
import com.greenlink.core.application.sms.dto.BarobillKakaoTemplatesResult;
import com.greenlink.core.application.sms.dto.BarobillQueryResult;
import com.greenlink.core.application.sms.dto.BarobillUrlResult;
import com.greenlink.core.domain.sms.model.KakaoUrlType;

public interface BarobillQueryUseCase {

    /** companyId가 null이면 운영자 기본 계정 기준으로 발신번호 등록 여부를 조회한다. */
    BarobillQueryResult checkSenderNumber(Long companyId);

    BarobillQueryResult getSendState(Long companyId, String sendKey);

    /** 알림톡은 발주사 단위만 지원하므로 companyId 가 필수다. */
    BarobillKakaoChannelsResult getKakaoChannels(Long companyId);

    BarobillKakaoTemplatesResult getKakaoTemplates(Long companyId, String channelId);

    BarobillUrlResult getKakaoManagementUrl(Long companyId, KakaoUrlType type);
}
