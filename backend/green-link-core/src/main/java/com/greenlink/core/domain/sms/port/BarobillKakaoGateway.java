package com.greenlink.core.domain.sms.port;

import com.greenlink.core.domain.sms.model.KakaoChannel;
import com.greenlink.core.domain.sms.model.KakaoTemplate;
import com.greenlink.core.domain.sms.model.KakaoUrlType;
import com.greenlink.core.domain.sms.model.SmsAccount;

import java.util.List;

public interface BarobillKakaoGateway {

    /** errorCode 0 = 정상, 음수 = 바로빌 오류코드. 통신 실패 시 BusinessException. */
    KakaoLookup<KakaoChannel> getChannels(SmsAccount account);

    KakaoLookup<KakaoTemplate> getTemplates(SmsAccount account, String channelId);

    KakaoUrl getManagementUrl(SmsAccount account, KakaoUrlType type);

    record KakaoLookup<T>(int errorCode, List<T> items) {
        public static <T> KakaoLookup<T> ok(List<T> items) {
            return new KakaoLookup<>(0, items);
        }

        public static <T> KakaoLookup<T> error(int errorCode) {
            return new KakaoLookup<>(errorCode, List.of());
        }
    }

    record KakaoUrl(int errorCode, String url) {}
}
