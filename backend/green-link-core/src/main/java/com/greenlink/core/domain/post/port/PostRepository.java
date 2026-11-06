package com.greenlink.core.domain.post.port;

import com.greenlink.core.domain.post.model.Post;
import com.greenlink.core.domain.post.model.PostAttachment;
import com.greenlink.core.domain.post.model.PostSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    PostAttachment saveAttachment(PostAttachment attachment);
    Optional<Post> findById(Long id);
    Page<Post> search(PostSearchCondition condition, Pageable pageable);
    List<Post> findActivePinnedNotices();
    void incrementViewCount(Long id);
    void softDelete(Long id);
    List<PostAttachment> findAttachmentsByPostId(Long postId);
}
