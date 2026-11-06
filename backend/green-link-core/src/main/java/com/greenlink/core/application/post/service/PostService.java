package com.greenlink.core.application.post.service;

import com.greenlink.core.application.post.dto.CreatePostCommand;
import com.greenlink.core.application.post.dto.PostAttachmentResult;
import com.greenlink.core.application.post.dto.PostResult;
import com.greenlink.core.application.post.dto.PostSummaryResult;
import com.greenlink.core.application.post.dto.UpdateNoticeSettingsCommand;
import com.greenlink.core.application.post.dto.UpdatePostCommand;
import com.greenlink.core.application.post.usecase.CreatePostUseCase;
import com.greenlink.core.application.post.usecase.DeletePostUseCase;
import com.greenlink.core.application.post.usecase.GetPostUseCase;
import com.greenlink.core.application.post.usecase.MultipartFileData;
import com.greenlink.core.application.post.usecase.UpdateNoticeSettingsUseCase;
import com.greenlink.core.application.post.usecase.UpdatePostUseCase;
import com.greenlink.core.domain.post.model.Post;
import com.greenlink.core.domain.post.model.PostAttachment;
import com.greenlink.core.domain.post.model.PostSearchCondition;
import com.greenlink.core.domain.post.port.PostAttachmentStorage;
import com.greenlink.core.domain.post.port.PostRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.post.PostErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService implements CreatePostUseCase, GetPostUseCase, UpdatePostUseCase,
        DeletePostUseCase, UpdateNoticeSettingsUseCase {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PostRepository postRepository;
    private final PostAttachmentStorage postAttachmentStorage;

    public PostService(PostRepository postRepository, PostAttachmentStorage postAttachmentStorage) {
        this.postRepository = postRepository;
        this.postAttachmentStorage = postAttachmentStorage;
    }

    @Override
    @Transactional
    public PostResult createPost(CreatePostCommand command, List<MultipartFileData> files) {
        Post post = Post.create(
                command.authorId(),
                command.companyName(),
                command.title(),
                command.content(),
                command.isNotice(),
                command.isPinned(),
                command.noticeStartAt(),
                command.noticeEndAt()
        );
        Post saved = postRepository.save(post);

        if (files != null) {
            for (MultipartFileData file : files) {
                PostAttachmentStorage.UploadResult uploaded = postAttachmentStorage.store(
                        saved.getId(), file.originalFileName(), file.content(), file.contentType());
                PostAttachment attachment = PostAttachment.of(
                        saved.getId(), file.originalFileName(), uploaded.key(), uploaded.url());
                postRepository.saveAttachment(attachment);
            }
        }

        List<PostAttachment> attachments = postRepository.findAttachmentsByPostId(saved.getId());
        return toResult(saved, attachments);
    }

    @Override
    public Page<PostSummaryResult> getPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.search(condition, pageable).map(post -> {
            List<PostAttachment> attachments = postRepository.findAttachmentsByPostId(post.getId());
            return toSummaryResult(post, !attachments.isEmpty());
        });
    }

    @Override
    @Transactional
    public PostResult getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));
        postRepository.incrementViewCount(id);
        List<PostAttachment> attachments = postRepository.findAttachmentsByPostId(id);
        return toResult(post, attachments);
    }

    @Override
    @Transactional
    public PostResult updatePost(UpdatePostCommand command) {
        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!command.isAdmin() && !post.getAuthorId().equals(command.requesterId())) {
            throw new BusinessException(PostErrorCode.POST_FORBIDDEN);
        }

        post.updateContent(command.title(), command.content());
        Post saved = postRepository.save(post);
        List<PostAttachment> attachments = postRepository.findAttachmentsByPostId(saved.getId());
        return toResult(saved, attachments);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long requesterId, boolean isAdmin) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (!isAdmin && !post.getAuthorId().equals(requesterId)) {
            throw new BusinessException(PostErrorCode.POST_FORBIDDEN);
        }

        postRepository.softDelete(postId);
    }

    @Override
    @Transactional
    public void updateNoticeSettings(UpdateNoticeSettingsCommand command) {
        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        post.updateNoticeSettings(
                command.isNotice(),
                command.isPinned(),
                command.noticeStartAt(),
                command.noticeEndAt()
        );
        postRepository.save(post);
    }

    private PostResult toResult(Post post, List<PostAttachment> attachments) {
        List<PostAttachmentResult> attachmentResults = attachments.stream()
                .map(a -> new PostAttachmentResult(a.getId(), a.getFileName(), a.getUrl()))
                .toList();

        return new PostResult(
                post.getId(),
                post.getAuthorId(),
                post.getCompanyName(),
                post.getTitle(),
                post.getContent(),
                post.isNotice(),
                post.isPinned(),
                post.getNoticeStartAt() != null ? post.getNoticeStartAt().format(FORMATTER) : null,
                post.getNoticeEndAt() != null ? post.getNoticeEndAt().format(FORMATTER) : null,
                post.getViewCount(),
                post.getCreatedAt(),
                attachmentResults
        );
    }

    private PostSummaryResult toSummaryResult(Post post, boolean hasAttachment) {
        return new PostSummaryResult(
                post.getId(),
                post.getAuthorId(),
                post.getCompanyName(),
                post.getTitle(),
                post.isNotice(),
                post.isPinned(),
                post.getNoticeEndAt() != null ? post.getNoticeEndAt().format(FORMATTER) : null,
                post.getViewCount(),
                hasAttachment,
                post.getCreatedAt()
        );
    }
}
