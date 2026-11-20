package com.greenlink.core.domain.sms.port;

import com.greenlink.core.domain.sms.model.SmsAccount;

public interface BarobillGateway {

    /** 1 = 등록됨, 2 = 등록되지 않음, 음수 = 바로빌 오류코드. 통신 실패 시 BusinessException. */
    int checkSenderNumber(SmsAccount account);

    /** 0 = 전송중, 1 = 전송완료, 음수 = 실패코드. 통신 실패 시 BusinessException. */
    int getSendState(SmsAccount account, String sendKey);
}
