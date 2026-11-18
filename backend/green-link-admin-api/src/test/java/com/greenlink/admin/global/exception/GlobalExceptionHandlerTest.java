package com.greenlink.admin.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 공백_검증_실패는_400과_한글_라벨_메시지로_응답한다() throws Exception {
        ResponseEntity<ErrorResponse> response = validate(error("ordererName", "NotBlank", "must not be blank"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_PARAM");
        assertThat(response.getBody().message()).isEqualTo("주문자 성함: 필수 입력 항목입니다");
    }

    @Test
    void NotNull은_필수_입력_문구로_응답한다() throws Exception {
        ResponseEntity<ErrorResponse> response = validate(error("vendorId", "NotNull", "must not be null"));

        assertThat(response.getBody().message()).isEqualTo("수주업체: 필수 입력 항목입니다");
    }

    @Test
    void Min은_최솟값을_안내한다() throws Exception {
        ResponseEntity<ErrorResponse> response = validate(error("price", "Min", "must be greater than or equal to 0", "r", 0L));

        assertThat(response.getBody().message()).isEqualTo("가격: 0 이상이어야 합니다");
    }

    @Test
    void Size는_최소_최대_조합별로_안내한다() throws Exception {
        assertThat(validate(error("newPassword", "Size", "x", "r", Integer.MAX_VALUE, 8)).getBody().message())
                .isEqualTo("새 비밀번호: 8자 이상이어야 합니다");
        assertThat(validate(error("productName", "Size", "x", "r", 100, 0)).getBody().message())
                .isEqualTo("상품명: 100자 이하여야 합니다");
        assertThat(validate(error("password", "Size", "x", "r", 100, 4)).getBody().message())
                .isEqualTo("비밀번호: 4~100자여야 합니다");
    }

    @Test
    void 중첩_필드는_인덱스를_제거하고_라벨을_찾는다() throws Exception {
        ResponseEntity<ErrorResponse> response = validate(error("items[0].productName", "NotBlank", "must not be blank"));

        assertThat(response.getBody().message()).isEqualTo("상품명: 필수 입력 항목입니다");
    }

    @Test
    void 라벨이_없는_필드는_필드명을_그대로_쓴다() throws Exception {
        ResponseEntity<ErrorResponse> response = validate(error("unknownField", "NotBlank", "must not be blank"));

        assertThat(response.getBody().message()).isEqualTo("unknownField: 필수 입력 항목입니다");
    }

    @Test
    void 여러_오류는_쉼표로_이어붙인다() throws Exception {
        ResponseEntity<ErrorResponse> response = validate(
                error("ordererName", "NotBlank", "must not be blank"),
                error("deliveryDate", "NotBlank", "must not be blank"));

        assertThat(response.getBody().message())
                .isEqualTo("주문자 성함: 필수 입력 항목입니다, 배송일: 필수 입력 항목입니다");
    }

    @Test
    void 요청_본문_형식_오류는_400으로_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.handleNotReadable(
                new HttpMessageNotReadableException("Cannot map `null` into type `boolean`", (org.springframework.http.HttpInputMessage) null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_PARAM");
        assertThat(response.getBody().message()).isEqualTo("요청 형식이 올바르지 않습니다.");
    }

    record Sample(@NotBlank String ordererName,
                  @Min(0) long price,
                  @Size(min = 8) String newPassword,
                  @Size(max = 5) String productName,
                  @Size(min = 4, max = 6) String password) {}

    @Test
    void 실제_Validator가_만든_FieldError로도_한글_메시지가_나온다() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        BindingResult result = new BeanPropertyBindingResult(new Sample("", -1, "abc", "여섯글자이상", "a"), "sample");
        validator.validate(result.getTarget(), result);
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("요청_본문_형식_오류는_400으로_응답한다"), -1);

        String message = handler.handleValidation(new MethodArgumentNotValidException(parameter, result))
                .getBody().message();

        assertThat(message)
                .contains("주문자 성함: 필수 입력 항목입니다")
                .contains("가격: 0 이상이어야 합니다")
                .contains("새 비밀번호: 8자 이상이어야 합니다")
                .contains("상품명: 5자 이하여야 합니다")
                .contains("비밀번호: 4~6자여야 합니다");
    }

    private FieldError error(String field, String code, String defaultMessage, Object... args) {
        return new FieldError("request", field, null, false, new String[]{code}, args, defaultMessage);
    }

    private ResponseEntity<ErrorResponse> validate(FieldError... errors) throws Exception {
        BindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        for (FieldError error : errors) {
            result.addError(error);
        }
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("요청_본문_형식_오류는_400으로_응답한다"), -1);
        return handler.handleValidation(new MethodArgumentNotValidException(parameter, result));
    }
}
