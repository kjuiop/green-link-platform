package com.greenlink.admin.adapter.out.persistence.post;

import com.greenlink.core.domain.post.model.Post;
import com.greenlink.core.domain.post.model.PostAttachment;
import com.greenlink.core.domain.post.model.PostSearchCondition;
import com.greenlink.core.domain.post.port.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;
    private final PostAttachmentJpaRepository postAttachmentJpaRepository;
    private final PostQueryRepository postQueryRepository;

    public PostRepositoryAdapter(PostJpaRepository postJpaRepository,
                                  PostAttachmentJpaRepository postAttachmentJpaRepository,
                                  PostQueryRepository postQueryRepository) {
        this.postJpaRepository = postJpaRepository;
        this.postAttachmentJpaRepository = postAttachmentJpaRepository;
        this.postQueryRepository = postQueryRepository;
    }

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(PostJpaEntity.from(post)).toDomain();
    }

    @Override
    public PostAttachment saveAttachment(PostAttachment attachment) {
        return postAttachmentJpaRepository.save(PostAttachmentJpaEntity.from(attachment)).toDomain();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .map(PostJpaEntity::toDomain);
    }

    @Override
    public Page<Post> search(PostSearchCondition condition, Pageable pageable) {
        return postQueryRepository.search(condition, pageable).map(PostJpaEntity::toDomain);
    }

    @Override
    public List<Post> findActivePinnedNotices() {
        return postQueryRepository.findActivePinnedNotices().stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void incrementViewCount(Long id) {
        postJpaRepository.incrementViewCount(id);
    }

    @Override
    public void softDelete(Long id) {
        postJpaRepository.softDelete(id, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    @Override
    public List<PostAttachment> findAttachmentsByPostId(Long postId) {
        return postAttachmentJpaRepository.findAllByPostId(postId).stream()
                .map(PostAttachmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Set<Long> findPostIdsHavingAttachments(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return Set.of();
        return postAttachmentJpaRepository.findDistinctPostIdsByPostIdIn(postIds);
    }
}
