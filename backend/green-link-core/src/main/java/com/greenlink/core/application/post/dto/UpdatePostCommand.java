package com.greenlink.core.application.post.dto;

public record UpdatePostCommand(
        Long postId,
        Long requesterId,
        boolean isAdmin,
        String title,
        String content
) {}
