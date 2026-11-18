package com.greenlink.admin.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 검증_실패는_400과_필드별_메시지로_응답한다() throws Exception {
        BindingResult result = new BeanPropertyBindingResult(new Object(), "updateOrderRequest");
        result.addError(new FieldError("updateOrderRequest", "ordererName", "공백일 수 없습니다"));
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("검증_실패는_400과_필드별_메시지로_응답한다"), -1);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(
                new MethodArgumentNotValidException(parameter, result));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_PARAM");
        assertThat(response.getBody().message()).isEqualTo("ordererName: 공백일 수 없습니다");
    }
}
