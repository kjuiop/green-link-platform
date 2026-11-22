package com.greenlink.core.application.coupang.service;

import com.greenlink.core.application.coupang.dto.CoupangSyncResult;
import com.greenlink.core.domain.coupang.model.CoupangCredential;
import com.greenlink.core.domain.coupang.model.CoupangCredentialSecret;
import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import com.greenlink.core.domain.coupang.model.CoupangSyncLog;
import com.greenlink.core.domain.coupang.model.CoupangTriggerType;
import com.greenlink.core.domain.coupang.port.CoupangApiException;
import com.greenlink.core.domain.coupang.port.CoupangApiPort;
import com.greenlink.core.domain.coupang.port.CoupangCredentialRepository;
import com.greenlink.core.domain.coupang.port.CoupangSyncLogRepository;
import com.greenlink.core.domain.customerorder.model.CustomerOrder;
import com.greenlink.core.domain.customerorder.port.CustomerOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoupangOrderCollectServiceTest {

    @Mock CoupangCredentialRepository credentialRepository;
    @Mock CustomerOrderRepository orderRepository;
    @Mock CoupangSyncLogRepository syncLogRepository;
    @Mock CoupangApiPort apiPort;
    @Mock CoupangSecretResolver secretResolver;

    CoupangOrderCollectService service;

    @BeforeEach
    void setUp() {
        service = new CoupangOrderCollectService(credentialRepository, orderRepository, syncLogRepository,
                apiPort, secretResolver, 10, 12);
    }

    private CoupangCredential credential(LocalDate issuedAt) {
        return CoupangCredential.create(5L, "A001", "enc-a", "enc-s", issuedAt, true);
    }

    private CoupangOrderSheet sheet(long shipmentBoxId) {
        return new CoupangOrderSheet(1L, shipmentBoxId, "ACCEPT", LocalDateTime.now(), "a", "b", "c", "d", "e", "f",
                "g", List.of(), 1000, "{}");
    }

    @Test
    void 신규_주문만_저장하고_중복은_건너뛴다() {
        CoupangCredential credential = credential(LocalDate.now());
        given(credentialRepository.findByCompanyId(5L)).willReturn(Optional.of(credential));
        given(secretResolver.resolve(credential)).willReturn(new CoupangCredentialSecret("A001", "ak", "sk"));
        given(apiPort.fetchNewOrderSheets(any(), any(), any())).willReturn(List.of(sheet(1), sheet(2)), List.of());
        given(orderRepository.insertIfAbsent(any(CustomerOrder.class))).willReturn(true, false);

        CoupangSyncResult result = service.collect(5L, CoupangTriggerType.MANUAL);

        assertThat(result.fetchedCount()).isEqualTo(2);
        assertThat(result.insertedCount()).isEqualTo(1);
        assertThat(credential.getLastCollectedAt()).isEqualTo(result.to());
        verify(credentialRepository).save(credential);
        ArgumentCaptor<CoupangSyncLog> log = ArgumentCaptor.forClass(CoupangSyncLog.class);
        verify(syncLogRepository).save(log.capture());
        assertThat(log.getValue().isSuccess()).isTrue();
    }

    @Test
    void 첫_수집은_24시간_미만의_연속된_조각으로_나눠_호출한다() {
        CoupangCredential credential = credential(LocalDate.now());
        given(credentialRepository.findByCompanyId(5L)).willReturn(Optional.of(credential));
        given(secretResolver.resolve(credential)).willReturn(new CoupangCredentialSecret("A001", "ak", "sk"));
        given(apiPort.fetchNewOrderSheets(any(), any(), any())).willReturn(List.of());

        CoupangSyncResult result = service.collect(5L, CoupangTriggerType.MANUAL);

        ArgumentCaptor<LocalDateTime> from = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> to = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(apiPort, times(2)).fetchNewOrderSheets(any(), from.capture(), to.capture());
        assertThat(from.getAllValues().get(0)).isEqualTo(result.from());
        assertThat(to.getAllValues().get(0)).isEqualTo(from.getAllValues().get(1));
        assertThat(to.getAllValues().get(1)).isEqualTo(result.to());
        for (int i = 0; i < 2; i++) {
            assertThat(Duration.between(from.getAllValues().get(i), to.getAllValues().get(i)))
                    .isLessThan(Duration.ofDays(1));
        }
    }

    @Test
    void 쿠팡_호출이_실패하면_수집_시각을_갱신하지_않고_오류를_기록한다() {
        CoupangCredential credential = credential(LocalDate.now());
        given(credentialRepository.findByCompanyId(5L)).willReturn(Optional.of(credential));
        given(secretResolver.resolve(credential)).willReturn(new CoupangCredentialSecret("A001", "ak", "sk"));
        given(apiPort.fetchNewOrderSheets(any(), any(), any())).willThrow(new CoupangApiException(403, "forbidden"));

        assertThatThrownBy(() -> service.collect(5L, CoupangTriggerType.SCHEDULED))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CoupangErrorCode.COLLECT_FAILED);

        assertThat(credential.getLastCollectedAt()).isNull();
        assertThat(credential.getLastError()).contains("403");
        ArgumentCaptor<CoupangSyncLog> log = ArgumentCaptor.forClass(CoupangSyncLog.class);
        verify(syncLogRepository).save(log.capture());
        assertThat(log.getValue().isSuccess()).isFalse();
    }

    @Test
    void 만료된_키는_쿠팡을_호출하지_않는다() {
        CoupangCredential credential = credential(LocalDate.now().minusDays(200));
        given(credentialRepository.findByCompanyId(5L)).willReturn(Optional.of(credential));

        assertThatThrownBy(() -> service.collect(5L, CoupangTriggerType.SCHEDULED))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(CoupangErrorCode.KEY_EXPIRED);

        verify(apiPort, never()).fetchNewOrderSheets(any(), any(), any());
    }

    @Test
    void 연동_정보가_없으면_예외() {
        given(credentialRepository.findByCompanyId(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.collect(5L, CoupangTriggerType.MANUAL))
                .isInstanceOf(BusinessException.class);
    }
}
