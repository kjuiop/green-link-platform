package com.greenlink.core.application.post.usecase;

import com.greenlink.core.application.post.dto.PostResult;
import com.greenlink.core.application.post.dto.PostSummaryResult;
import com.greenlink.core.domain.post.model.PostSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetPostUseCase {
    Page<PostSummaryResult> getPosts(PostSearchCondition condition, Pageable pageable);
    PostResult getPost(Long id);
}
