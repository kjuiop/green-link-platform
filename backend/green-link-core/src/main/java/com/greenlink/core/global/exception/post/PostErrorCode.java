package com.greenlink.core.global.exception.post;

import com.greenlink.core.global.exception.ErrorCode;

public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(404, "POST_001", "게시글을 찾을 수 없습니다."),
    POST_FORBIDDEN(403, "POST_002", "게시글 수정/삭제 권한이 없습니다."),
    POST_UPLOAD_FAILED(500, "POST_003", "첨부파일 업로드에 실패했습니다.");

    private final int status;
    private final String code;
    private final String message;

    PostErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
