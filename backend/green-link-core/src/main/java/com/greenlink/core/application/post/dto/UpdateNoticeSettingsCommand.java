package com.greenlink.core.application.post.dto;

import java.time.LocalDateTime;

public record UpdateNoticeSettingsCommand(
        Long postId,
        boolean isNotice,
        boolean isPinned,
        LocalDateTime noticeStartAt,
        LocalDateTime noticeEndAt
) {}
