package com.greenlink.core.domain.coupang.port;

public class CoupangApiException extends RuntimeException {

    private final int httpStatus;

    public CoupangApiException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public CoupangApiException(int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() { return httpStatus; }

    public boolean isAuthProblem() { return httpStatus == 401 || httpStatus == 403; }

    public String toUserMessage() {
        if (isAuthProblem()) {
            return "쿠팡 인증에 실패했습니다. 키 만료, 키 값 오류, 서버 IP 미등록 여부를 확인해 주세요. (HTTP " + httpStatus + ")";
        }
        return "쿠팡 API 호출에 실패했습니다. (" + (httpStatus > 0 ? "HTTP " + httpStatus : "연결 오류") + ") " + getMessage();
    }
}
