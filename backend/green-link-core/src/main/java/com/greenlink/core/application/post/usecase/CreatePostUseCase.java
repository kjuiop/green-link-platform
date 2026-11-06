package com.greenlink.core.application.post.usecase;

import com.greenlink.core.application.post.dto.CreatePostCommand;
import com.greenlink.core.application.post.dto.PostResult;

import java.util.List;

public interface CreatePostUseCase {
    PostResult createPost(CreatePostCommand command, List<MultipartFileData> files);
}
