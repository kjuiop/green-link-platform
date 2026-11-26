package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.application.vendor.dto.VendorSignupWithDocumentsResult;
import com.greenlink.core.application.vendor.usecase.UploadVendorDocumentUseCase;
import com.greenlink.core.application.vendor.usecase.VendorSignupUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.photo.PhotoErrorCode;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class VendorSignupWithDocumentsServiceTest {

    @Mock VendorSignupUseCase vendorSignupUseCase;
    @Mock UploadVendorDocumentUseCase uploadVendorDocumentUseCase;

    private VendorSignupWithDocumentsService service;

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private final VendorSignupCommand cmd = new VendorSignupCommand(
            "vendor01", "pw", "홍길동", "010-1234-5678", null, "123-45-67890", "테스트꽃집",
            "홍대표", null, null, null, List.of("VENDOR"), List.of(), List.of());
    private final SignupDocument businessReg = new SignupDocument("biz.png", PNG, "image/png");
    private final SignupDocument bankBook = new SignupDocument("bank.png", PNG, "image/png");

    @BeforeEach
    void setUp() {
        service = new VendorSignupWithDocumentsService(
                vendorSignupUseCase, uploadVendorDocumentUseCase, new SignupDocumentValidator());
    }

    private VendorResult vendor(String businessRegUrl, String bankBookUrl) {
        return new VendorResult(10L, "테스트꽃집", "123-45-67890", null, null, "홍길동", "010-1234-5678",
                null, null, null, "vendor01", "PENDING", null, "VENDOR", List.of(), List.of(),
                businessRegUrl, bankBookUrl, null, null, null, null);
    }

    @Test
    void 가입_후_서류_두_건을_업로드한다() {
        given(vendorSignupUseCase.signup(cmd)).willReturn(vendor(null, null));

        VendorSignupWithDocumentsResult result = service.signup(cmd, businessReg, bankBook);

        assertThat(result.failedDocuments()).isEmpty();
        assertThat(result.vendor().id()).isEqualTo(10L);
        verify(uploadVendorDocumentUseCase).uploadDocument(10L, "businessReg", "biz.png", PNG, "image/png");
        verify(uploadVendorDocumentUseCase).uploadDocument(10L, "bankBook", "bank.png", PNG, "image/png");
    }

    @Test
    void 서류가_유효하지_않으면_가입하지_않는다() {
        SignupDocument invalid = new SignupDocument("a.exe", PNG, "application/octet-stream");

        assertThatThrownBy(() -> service.signup(cmd, businessReg, invalid))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(VendorErrorCode.DOCUMENT_INVALID_TYPE));
        verifyNoInteractions(vendorSignupUseCase, uploadVendorDocumentUseCase);
    }

    @Test
    void 서류가_없으면_가입하지_않는다() {
        assertThatThrownBy(() -> service.signup(cmd, null, bankBook))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(VendorErrorCode.DOCUMENT_REQUIRED));
        verifyNoInteractions(vendorSignupUseCase, uploadVendorDocumentUseCase);
    }

    @Test
    void 이미_서류가_있는_회사는_해당_서류를_덮어쓰지_않는다() {
        given(vendorSignupUseCase.signup(cmd)).willReturn(vendor("https://s3/biz.png", null));

        VendorSignupWithDocumentsResult result = service.signup(cmd, businessReg, bankBook);

        assertThat(result.failedDocuments()).isEmpty();
        verify(uploadVendorDocumentUseCase, never())
                .uploadDocument(anyLong(), eq("businessReg"), anyString(), any(), any());
        verify(uploadVendorDocumentUseCase).uploadDocument(10L, "bankBook", "bank.png", PNG, "image/png");
    }

    @Test
    void 업로드가_실패해도_가입_결과를_반환하고_실패_서류를_알린다() {
        given(vendorSignupUseCase.signup(cmd)).willReturn(vendor(null, null));
        given(uploadVendorDocumentUseCase.uploadDocument(10L, "businessReg", "biz.png", PNG, "image/png"))
                .willThrow(new BusinessException(PhotoErrorCode.PHOTO_UPLOAD_FAILED));

        VendorSignupWithDocumentsResult result = service.signup(cmd, businessReg, bankBook);

        assertThat(result.vendor().id()).isEqualTo(10L);
        assertThat(result.failedDocuments()).containsExactly("businessReg");
        verify(uploadVendorDocumentUseCase).uploadDocument(10L, "bankBook", "bank.png", PNG, "image/png");
    }

    @Test
    void 업로드에서_예상치_못한_런타임_예외가_나도_가입은_유지한다() {
        given(vendorSignupUseCase.signup(cmd)).willReturn(vendor(null, null));
        given(uploadVendorDocumentUseCase.uploadDocument(10L, "businessReg", "biz.png", PNG, "image/png"))
                .willReturn(new UploadVendorDocumentUseCase.Result("https://s3/biz.png", null));
        given(uploadVendorDocumentUseCase.uploadDocument(10L, "bankBook", "bank.png", PNG, "image/png"))
                .willThrow(new IllegalStateException("s3 down"));

        VendorSignupWithDocumentsResult result = service.signup(cmd, businessReg, bankBook);

        assertThat(result.failedDocuments()).containsExactly("bankBook");
    }

    @Test
    void 클라이언트가_보낸_content_type이_아닌_판별된_값으로_업로드한다() {
        given(vendorSignupUseCase.signup(cmd)).willReturn(vendor(null, null));
        SignupDocument spoofed = new SignupDocument("biz.png", PNG, "text/html");

        service.signup(cmd, spoofed, bankBook);

        verify(uploadVendorDocumentUseCase).uploadDocument(10L, "businessReg", "biz.png", PNG, "image/png");
    }

    @Test
    void 가입이_실패하면_업로드하지_않는다() {
        given(vendorSignupUseCase.signup(cmd))
                .willThrow(new BusinessException(VendorErrorCode.DUPLICATE_LOGIN_ID));

        assertThatThrownBy(() -> service.signup(cmd, businessReg, bankBook))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(uploadVendorDocumentUseCase);
    }
}
