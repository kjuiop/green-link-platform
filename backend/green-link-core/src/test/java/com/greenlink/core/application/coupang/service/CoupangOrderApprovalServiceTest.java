package com.greenlink.core.application.coupang.service;

import com.greenlink.core.domain.coupang.model.CoupangCredential;
import com.greenlink.core.domain.coupang.model.CoupangCredentialSecret;
import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import com.greenlink.core.domain.coupang.port.CoupangApiException;
import com.greenlink.core.domain.coupang.port.CoupangApiPort;
import com.greenlink.core.domain.coupang.port.CoupangCredentialRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoupangOrderApprovalServiceTest {

    @Mock CoupangOrderStateService stateService;
    @Mock CoupangCredentialRepository credentialRepository;
    @Mock CoupangApiPort apiPort;
    @Mock CoupangSecretResolver secretResolver;

    CoupangOrderApprovalService service;
    CoupangOrder order;

    @BeforeEach
    void setUp() {
        service = new CoupangOrderApprovalService(stateService, credentialRepository, apiPort, secretResolver, true);
        order = CoupangOrder.of(3L, 5L, 100L, 200L, com.greenlink.core.domain.coupang.model.CoupangOrderStatus.PENDING,
                "ACCEPT", LocalDateTime.now(), "a", "b", "c", "d", "e", "f", "g", List.of(), 1000, "{}", null, null,
                null, LocalDateTime.now());
        CoupangCredential credential = CoupangCredential.create(5L, "A001", "x", "y", LocalDate.now(), true);
        given(stateService.loadApprovable(5L, 3L)).willReturn(order);
        given(credentialRepository.findByCompanyId(5L)).willReturn(Optional.of(credential));
        given(secretResolver.resolve(credential)).willReturn(new CoupangCredentialSecret("A001", "ak", "sk"));
    }

    private CoupangOrderSheet liveSheet(String status) {
        return new CoupangOrderSheet(100L, 200L, status, LocalDateTime.now(), "a", "b", "c", "d", "e", "f", "g",
                List.of(), 1000, "{}");
    }

    @Test
    void 쿠팡에서_주문이_사라졌으면_취소처리하고_승인하지_않는다() {
        given(apiPort.fetchOrderSheet(any(), anyLong(), anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(5L, 3L, cid -> null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CoupangErrorCode.CANCELED_ON_COUPANG);

        verify(stateService).markCanceled(3L);
        verify(stateService, never()).approve(any(), anyLong(), any(), any());
    }

    @Test
    void 결제완료가_아닌_상태면_승인하지_않는다() {
        given(apiPort.fetchOrderSheet(any(), anyLong(), anyLong())).willReturn(Optional.of(liveSheet("CANCEL")));

        assertThatThrownBy(() -> service.approve(5L, 3L, cid -> null)).isInstanceOf(BusinessException.class);

        verify(stateService, never()).approve(any(), anyLong(), any(), any());
    }

    @Test
    void 쿠팡_호출_실패시_VERIFY_FAILED() {
        given(apiPort.fetchOrderSheet(any(), anyLong(), anyLong())).willThrow(new CoupangApiException(500, "boom"));

        assertThatThrownBy(() -> service.approve(5L, 3L, cid -> null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CoupangErrorCode.VERIFY_FAILED);

        verify(stateService, never()).markCanceled(anyLong());
    }

    @Test
    void 정상이면_상태_서비스에_위임한다() {
        given(apiPort.fetchOrderSheet(any(), anyLong(), anyLong())).willReturn(Optional.of(liveSheet("ACCEPT")));

        service.approve(5L, 3L, cid -> null);

        verify(stateService).approve(any(), anyLong(), any(), any());
    }
}
