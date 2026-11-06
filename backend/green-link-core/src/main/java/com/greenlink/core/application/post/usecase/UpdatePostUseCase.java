package com.greenlink.core.application.post.usecase;

import com.greenlink.core.application.post.dto.PostResult;
import com.greenlink.core.application.post.dto.UpdatePostCommand;

public interface UpdatePostUseCase {
    PostResult updatePost(UpdatePostCommand command);
}
