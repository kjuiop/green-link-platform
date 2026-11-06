package com.greenlink.core.application.post.usecase;

public interface DeletePostUseCase {
    void deletePost(Long postId, Long requesterId, boolean isAdmin);
}
