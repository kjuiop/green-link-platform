package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.post.dto.CreatePostCommand;
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
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.post.model.PostSearchCondition;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.post.PostErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CreatePostUseCase createPostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final UpdateNoticeSettingsUseCase updateNoticeSettingsUseCase;
    private final CompanyRepository companyRepository;

    public PostController(CreatePostUseCase createPostUseCase,
                          GetPostUseCase getPostUseCase,
                          UpdatePostUseCase updatePostUseCase,
                          DeletePostUseCase deletePostUseCase,
                          UpdateNoticeSettingsUseCase updateNoticeSettingsUseCase,
                          CompanyRepository companyRepository) {
        this.createPostUseCase = createPostUseCase;
        this.getPostUseCase = getPostUseCase;
        this.updatePostUseCase = updatePostUseCase;
        this.deletePostUseCase = deletePostUseCase;
        this.updateNoticeSettingsUseCase = updateNoticeSettingsUseCase;
        this.companyRepository = companyRepository;
    }

    @GetMapping
    public ResponseEntity<Page<PostSummaryResult>> getPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean noticeOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PostSearchCondition condition = new PostSearchCondition(keyword, noticeOnly);
        return ResponseEntity.ok(getPostUseCase.getPosts(condition, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResult> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(getPostUseCase.getPost(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResult> createPost(
            @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {

        Long authorId = extractMemberId(authentication);
        Long companyId = extractCompanyId(authentication);
        String companyName = resolveCompanyName(companyId);

        List<MultipartFileData> fileData;
        try {
            fileData = toFileDataList(files);
        } catch (IOException e) {
            throw new BusinessException(PostErrorCode.POST_UPLOAD_FAILED);
        }

        CreatePostCommand command = new CreatePostCommand(
                authorId,
                companyName,
                request.title(),
                request.content(),
                request.isNotice(),
                request.isPinned(),
                parseDateTime(request.noticeStartAt()),
                parseDateTime(request.noticeEndAt())
        );

        PostResult result = createPostUseCase.createPost(command, fileData);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResult> updatePost(
            @PathVariable Long id,
            @RequestPart("data") UpdatePostRequest request,
            Authentication authentication) {

        Long requesterId = extractMemberId(authentication);
        boolean isAdmin = isSuperAdmin(authentication);

        UpdatePostCommand command = new UpdatePostCommand(
                id,
                requesterId,
                isAdmin,
                request.title(),
                request.content()
        );

        return ResponseEntity.ok(updatePostUseCase.updatePost(command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            Authentication authentication) {

        Long requesterId = extractMemberId(authentication);
        boolean isAdmin = isSuperAdmin(authentication);

        deletePostUseCase.deletePost(id, requesterId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/notice")
    public ResponseEntity<Void> updateNoticeSettings(
            @PathVariable Long id,
            @RequestBody UpdateNoticeSettingsRequest request) {

        UpdateNoticeSettingsCommand command = new UpdateNoticeSettingsCommand(
                id,
                request.isNotice(),
                request.isPinned(),
                parseDateTime(request.noticeStartAt()),
                parseDateTime(request.noticeEndAt())
        );

        updateNoticeSettingsUseCase.updateNoticeSettings(command);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----

    private Long extractMemberId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    private Long extractCompanyId(Authentication authentication) {
        if (authentication instanceof UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long companyId) {
            return companyId;
        }
        return null;
    }

    private String resolveCompanyName(Long companyId) {
        if (companyId == null) return "";
        return companyRepository.findById(companyId)
                .map(c -> c.getCompanyName())
                .orElse("");
    }

    private boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDateTime.parse(value, FORMATTER);
    }

    private List<MultipartFileData> toFileDataList(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return List.of();
        List<MultipartFileData> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(new MultipartFileData(
                    file.getOriginalFilename(),
                    file.getBytes(),
                    file.getContentType()
            ));
        }
        return result;
    }

    // ---- inner request records ----

    public record CreatePostRequest(
            String title,
            String content,
            boolean isNotice,
            boolean isPinned,
            String noticeStartAt,
            String noticeEndAt
    ) {}

    public record UpdatePostRequest(String title, String content) {}

    public record UpdateNoticeSettingsRequest(
            boolean isNotice,
            boolean isPinned,
            String noticeStartAt,
            String noticeEndAt
    ) {}
}
