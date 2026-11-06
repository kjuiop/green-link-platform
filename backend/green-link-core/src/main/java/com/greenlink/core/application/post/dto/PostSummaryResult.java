package com.greenlink.core.application.post.dto;

public record PostSummaryResult(
        Long id,
        Long authorId,
        String companyName,
        String title,
        boolean isNotice,
        boolean isPinned,
        String noticeEndAt,
        int viewCount,
        boolean hasAttachment,
        String createdAt
) {}
