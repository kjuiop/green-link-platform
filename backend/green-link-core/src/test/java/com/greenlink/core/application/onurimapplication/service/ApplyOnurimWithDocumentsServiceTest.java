package com.greenlink.core.application.onurimapplication.service;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.dto.OnurimDocumentRequirement;
import com.greenlink.core.application.onurimapplication.usecase.ApplyOnurimUseCase;
import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.application.vendor.service.SignupDocumentValidator;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.DocumentStorage;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.ErrorCode;
import com.greenlink.core.global.exception.onurimapplication.OnurimApplicationErrorCode;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ApplyOnurimWithDocumentsServiceTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long COMPANY_ID = 7L;
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Mock ApplyOnurimUseCase applyUseCase;
    @Mock DocumentStorage storage;

    private ApplyOnurimWithDocumentsService service;
    private final OnurimApplicationInfo info =
        new OnurimApplicationInfo("02-123-4567", "서울", null, null, null, null, null, null, null, List.of());

    @BeforeEach
    void setUp() {
        service = new ApplyOnurimWithDocumentsService(applyUseCase, new SignupDocumentValidator(), storage);
    }

    private static SignupDocument png(String name) {
        return new SignupDocument(name, PNG.clone(), "application/octet-stream");
    }

    private static void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run)
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    private void need(boolean biz, boolean bank) {
        given(applyUseCase.getDocumentRequirement(ADMIN_ID, COMPANY_ID)).willReturn(new OnurimDocumentRequirement(biz, bank));
    }

    private OnurimApplicationResult call(SignupDocument biz, SignupDocument bank) {
        return service.apply(ADMIN_ID, COMPANY_ID, CompanyType.BOTH, info, biz, bank);
    }

    @Test
    void 두_서류가_모두_필요하면_업로드_후_URL로_신청한다() {
        need(true, true);
        given(storage.store(eq(COMPANY_ID), eq("businessReg"), anyString(), any(), eq("image/png")))
            .willReturn(new DocumentStorage.UploadResult("k1", "https://s3/biz"));
        given(storage.store(eq(COMPANY_ID), eq("bankBook"), anyString(), any(), eq("image/png")))
            .willReturn(new DocumentStorage.UploadResult("k2", "https://s3/bank"));

        call(png("biz.png"), png("bank.png"));

        InOrder order = inOrder(applyUseCase, storage);
        order.verify(applyUseCase).ensureApplicable(ADMIN_ID, COMPANY_ID);
        order.verify(applyUseCase).getDocumentRequirement(ADMIN_ID, COMPANY_ID);
        order.verify(storage, org.mockito.Mockito.times(2)).store(anyLong(), anyString(), anyString(), any(), anyString());
        order.verify(applyUseCase).apply(ADMIN_ID, COMPANY_ID, CompanyType.BOTH, info, "https://s3/biz", "https://s3/bank");
    }

    @Test
    void 통장사본만_필요하면_그것만_업로드한다() {
        need(false, true);
        given(storage.store(eq(COMPANY_ID), eq("bankBook"), anyString(), any(), eq("image/png")))
            .willReturn(new DocumentStorage.UploadResult("k2", "https://s3/bank"));

        call(null, png("bank.png"));

        verify(storage, never()).store(anyLong(), eq("businessReg"), anyString(), any(), anyString());
        verify(applyUseCase).apply(ADMIN_ID, COMPANY_ID, CompanyType.BOTH, info, null, "https://s3/bank");
    }

    @Test
    void 두_서류가_불필요하면_업로드_없이_신청한다() {
        need(false, false);

        call(null, null);

        verifyNoInteractions(storage);
        verify(applyUseCase).apply(ADMIN_ID, COMPANY_ID, CompanyType.BOTH, info, null, null);
    }

    @Test
    void 필요한_서류가_빠지면_거부하고_아무것도_하지_않는다() {
        need(true, true);

        assertCode(() -> call(png("biz.png"), null), OnurimApplicationErrorCode.DOCUMENT_REQUIRED);

        verifyNoInteractions(storage);
        verify(applyUseCase, never()).apply(any(), any(), any(), any(), any(), any());
    }

    @Test
    void 불필요한_서류를_첨부하면_거부한다() {
        need(false, true);

        assertCode(() -> call(png("biz.png"), png("bank.png")), OnurimApplicationErrorCode.DOCUMENT_NOT_ALLOWED);

        verifyNoInteractions(storage);
        verify(applyUseCase, never()).apply(any(), any(), any(), any(), any(), any());
    }

    @Test
    void 파일_형식이_잘못되면_업로드하지_않는다() {
        need(true, true);
        SignupDocument fake = new SignupDocument("biz.png", "not-an-image".getBytes(), "image/png");

        assertCode(() -> call(fake, png("bank.png")), VendorErrorCode.DOCUMENT_INVALID_TYPE);

        verifyNoInteractions(storage);
        verify(applyUseCase, never()).apply(any(), any(), any(), any(), any(), any());
    }

    @Test
    void 이미_심사중이면_서류가_없어도_서류_오류보다_중복_오류를_먼저_낸다() {
        org.mockito.Mockito.doThrow(new BusinessException(OnurimApplicationErrorCode.ALREADY_PENDING))
            .when(applyUseCase).ensureApplicable(ADMIN_ID, COMPANY_ID);

        assertCode(() -> call(null, null), OnurimApplicationErrorCode.ALREADY_PENDING);

        verify(applyUseCase, never()).getDocumentRequirement(anyLong(), anyLong());
        verifyNoInteractions(storage);
    }

    @Test
    void 사전검증에_실패하면_업로드하지_않는다() {
        org.mockito.Mockito.doThrow(new BusinessException(OnurimApplicationErrorCode.ALREADY_PENDING))
            .when(applyUseCase).ensureApplicable(ADMIN_ID, COMPANY_ID);

        assertCode(() -> call(png("biz.png"), png("bank.png")), OnurimApplicationErrorCode.ALREADY_PENDING);

        verifyNoInteractions(storage);
    }

    @Test
    void 업로드가_실패하면_신청을_만들지_않는다() {
        need(true, true);
        given(storage.store(anyLong(), anyString(), anyString(), any(), anyString()))
            .willThrow(new RuntimeException("s3 down"));

        assertThatThrownBy(() -> call(png("biz.png"), png("bank.png"))).isInstanceOf(RuntimeException.class);

        verify(applyUseCase, never()).apply(any(), any(), any(), any(), any(), any());
    }

    @Test
    void 서류를_클라이언트_content_type이_아닌_서버_판정값으로_저장한다() {
        need(true, false);
        given(storage.store(eq(COMPANY_ID), eq("businessReg"), anyString(), any(), eq("image/png")))
            .willReturn(new DocumentStorage.UploadResult("k1", "https://s3/biz"));

        call(new SignupDocument("biz.png", PNG.clone(), "text/html"), null);

        verify(storage).store(eq(COMPANY_ID), eq("businessReg"), eq("biz.png"), any(), eq("image/png"));
        verify(applyUseCase).apply(eq(ADMIN_ID), eq(COMPANY_ID), any(), any(), eq("https://s3/biz"), isNull());
    }
}
