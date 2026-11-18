package com.greenlink.admin.global.exception;

import com.greenlink.core.global.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
        Map.entry("companyName", "업체명"),
        Map.entry("deliveryDate", "배송일"),
        Map.entry("deliveryTimeType", "배송시간 유형"),
        Map.entry("loginId", "아이디"),
        Map.entry("managerName", "담당자명"),
        Map.entry("name", "이름"),
        Map.entry("newPassword", "새 비밀번호"),
        Map.entry("ordererName", "주문자 성함"),
        Map.entry("ordererPhone", "주문자 연락처"),
        Map.entry("password", "비밀번호"),
        Map.entry("phone", "연락처"),
        Map.entry("price", "가격"),
        Map.entry("memo", "메모"),
        Map.entry("productName", "상품명"),
        Map.entry("quantity", "수량"),
        Map.entry("roles", "권한"),
        Map.entry("status", "상태"),
        Map.entry("unitPrice", "단가"),
        Map.entry("url", "경로"),
        Map.entry("vendorId", "수주업체")
    );

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        var errorCode = e.getErrorCode();
        return ResponseEntity
            .status(HttpStatus.valueOf(errorCode.getStatus()))
            .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(this::toMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_PARAM", message.isBlank() ? "잘못된 요청입니다." : message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("Unreadable request body: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_PARAM", "요청 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_PARAM", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_STATE", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."));
    }

    private String toMessage(FieldError f) {
        String field = f.getField().replaceAll("\\[\\d+]", "");
        String last = field.substring(field.lastIndexOf('.') + 1);
        String label = FIELD_LABELS.getOrDefault(last, last);
        Object[] args = f.getArguments() == null ? new Object[0] : f.getArguments();
        String reason = switch (f.getCode() == null ? "" : f.getCode()) {
            case "NotBlank", "NotNull" -> "필수 입력 항목입니다";
            case "Min" -> args.length > 1 ? args[1] + " 이상이어야 합니다" : "입력값 범위를 확인해 주세요";
            case "Size" -> sizeMessage(args);
            default -> f.getDefaultMessage();
        };
        return label + ": " + reason;
    }

    private String sizeMessage(Object[] args) {
        if (args.length < 3 || !(args[1] instanceof Number max) || !(args[2] instanceof Number min)) {
            return "입력값 길이를 확인해 주세요";
        }
        boolean hasMin = min.intValue() > 0;
        boolean hasMax = max.intValue() < Integer.MAX_VALUE;
        if (hasMin && hasMax) return min + "~" + max + "자여야 합니다";
        if (hasMin) return min + "자 이상이어야 합니다";
        if (hasMax) return max + "자 이하여야 합니다";
        return "입력값 길이를 확인해 주세요";
    }
}
