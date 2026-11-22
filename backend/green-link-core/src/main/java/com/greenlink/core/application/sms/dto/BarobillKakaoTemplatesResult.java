package com.greenlink.core.application.sms.dto;

import com.greenlink.core.domain.sms.model.KakaoTemplate;

import java.util.List;

public record BarobillKakaoTemplatesResult(int code, String message, List<KakaoTemplate> templates) {
}
