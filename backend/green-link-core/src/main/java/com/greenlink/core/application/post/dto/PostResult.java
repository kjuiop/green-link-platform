package com.greenlink.core.application.post.dto;

import java.util.List;

public record PostResult(
        Long id,
        Long authorId,
        String companyName,
        String title,
        String content,
        boolean isNotice,
        boolean isPinned,
        String noticeStartAt,
        String noticeEndAt,
        int viewCount,
        String createdAt,
        List<PostAttachmentResult> attachments
) {}
