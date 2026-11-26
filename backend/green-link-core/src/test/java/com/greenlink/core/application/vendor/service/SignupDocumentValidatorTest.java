package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignupDocumentValidatorTest {

    private final SignupDocumentValidator validator = new SignupDocumentValidator();

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31};
    private static final byte[] WEBP = {0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50};
    private static final byte[] HEIC = {0, 0, 0, 0x18, 0x66, 0x74, 0x79, 0x70, 0x68, 0x65, 0x69, 0x63};

    private SignupDocument doc(String name, byte[] content) {
        return new SignupDocument(name, content, "application/octet-stream");
    }

    private void assertError(SignupDocument doc, VendorErrorCode expected) {
        assertThatThrownBy(() -> validator.validate(doc))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 허용_형식은_통과한다() {
        assertThatCode(() -> {
            validator.validate(doc("a.png", PNG));
            validator.validate(doc("a.JPG", JPEG));
            validator.validate(doc("a.pdf", PDF));
            validator.validate(doc("a.webp", WEBP));
            validator.validate(doc("a.heic", HEIC));
        }).doesNotThrowAnyException();
    }

    @Test
    void null_또는_빈_파일은_필수_오류() {
        assertError(null, VendorErrorCode.DOCUMENT_REQUIRED);
        assertError(doc("a.png", new byte[0]), VendorErrorCode.DOCUMENT_REQUIRED);
        assertError(new SignupDocument("a.png", null, null), VendorErrorCode.DOCUMENT_REQUIRED);
    }

    @Test
    void 확장자가_허용되지_않으면_거부한다() {
        assertError(doc("a.exe", PNG), VendorErrorCode.DOCUMENT_INVALID_TYPE);
        assertError(doc("noext", PNG), VendorErrorCode.DOCUMENT_INVALID_TYPE);
        assertError(doc(null, PNG), VendorErrorCode.DOCUMENT_INVALID_TYPE);
    }

    @Test
    void 확장자와_무관하게_시그니처가_다르면_거부한다() {
        assertError(doc("a.png", "<script>alert(1)</script>".getBytes()), VendorErrorCode.DOCUMENT_INVALID_TYPE);
        assertError(doc("a.pdf", new byte[]{1, 2}), VendorErrorCode.DOCUMENT_INVALID_TYPE);
    }

    @Test
    void 십메가_초과는_거부한다() {
        byte[] big = new byte[SignupDocumentValidator.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, big, 0, PNG.length);
        assertError(doc("a.png", big), VendorErrorCode.DOCUMENT_TOO_LARGE);
    }

    @Test
    void 십메가_정확히는_통과한다() {
        byte[] exact = new byte[SignupDocumentValidator.MAX_BYTES];
        System.arraycopy(PNG, 0, exact, 0, PNG.length);
        assertThatCode(() -> validator.validate(doc("a.png", exact))).doesNotThrowAnyException();
    }
}
