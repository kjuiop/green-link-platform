package com.greenlink.core.application.sms.dto;

import com.greenlink.core.domain.sms.model.KakaoChannel;

import java.util.List;

public record BarobillKakaoChannelsResult(int code, String message, List<KakaoChannel> channels) {
}
