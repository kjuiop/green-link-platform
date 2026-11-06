package com.greenlink.core.application.post.dto;

import java.time.LocalDateTime;

public record CreatePostCommand(
        Long authorId,
        String companyName,
        String title,
        String content,
        boolean isNotice,
        boolean isPinned,
        LocalDateTime noticeStartAt,
        LocalDateTime noticeEndAt
) {}
