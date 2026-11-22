package com.greenlink.core.domain.sms.model;

import java.util.List;

public record KakaoTemplate(String templateName, String title, String content, int status,
                            List<KakaoButton> buttons) {}
