package com.greenlink.core.application.fax.service;

import com.greenlink.core.application.fax.dto.FaxMessageResult;
import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.fax.model.FaxMessage;
import com.greenlink.core.domain.fax.model.FaxStatus;
import com.greenlink.core.domain.fax.model.OrderSheet;
import com.greenlink.core.domain.fax.port.FaxGateway;
import com.greenlink.core.domain.fax.port.FaxGateway.FaxAccount;
import com.greenlink.core.domain.fax.port.FaxGateway.FaxSendResult;
import com.greenlink.core.domain.fax.port.FaxMessageRepository;
import com.greenlink.core.domain.fax.port.OrderSheetRenderer;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderItem;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderItemRepository;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.fax.FaxErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderFaxServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CompanyRepository companyRepository;
    @Mock FaxMessageRepository faxMessageRepository;
    @Mock FaxAccountResolver accountResolver;
    @Mock OrderSheetRenderer renderer;
    @Mock FaxGateway gateway;
    @Mock PlatformTransactionManager transactionManager;

    private OrderFaxService service;

    private static final FaxAccount ACCOUNT = new FaxAccount("key", "1234567890", "baro", "0212345678",
            null, "ftp.example.com", 21, "u", "p", "/");

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new OrderFaxService(orderRepository, orderItemRepository, companyRepository, faxMessageRepository,
                accountResolver, renderer, gateway, transactionManager);
    }

    private Order order(OrderStatus status, String address) {
        return Order.of(
                10L, "DF-20261120-0001", 1L, 100L, 200L, status,
                null, "장미", 1, 50000L, null, null, 50000L,
                "2026-11-21", "FIXED", "14", "00", null,
                address, "101호", "생일", "그랜드볼룸", "A홀", null,
                "축하합니다", "홍길동", null, "홍길동", "010-1234-5678",
                "김영희", "010-9876-5432", true, true,
                "SCHEDULED", "13", "00", null, null, null, null, "2026-11-20T10:00:00", null, null, false, null);
    }

    private Company company(Long id, boolean faxEnabled, String fax) {
        Company c = Company.of(id, "000-00-0000" + id, "업체" + id, null, "02-000-000" + id, fax, null, null,
                CompanyType.VENDOR, CompanyStatus.ACTIVE, true, null, null, null, null, null, BusinessStatus.OPEN, 3000L);
        c.restoreFaxEnabled(faxEnabled);
        return c;
    }

    private void stubEnvironment(Order order) {
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));
        given(companyRepository.findById(100L)).willReturn(Optional.of(company(100L, true, "02-111-2222")));
        lenient().when(companyRepository.findById(200L)).thenReturn(Optional.of(company(200L, false, null)));
        given(orderItemRepository.findAllByOrderId(10L)).willReturn(List.of(OrderItem.create(null, "장미", 50000L, 1)));
    }

    private String hashOf(Order order) {
        Company ordering = company(200L, false, null);
        Company vendor = company(100L, true, "02-111-2222");
        return OrderSheetFactory.create(order, List.of(OrderItem.create(null, "장미", 50000L, 1)), ordering, vendor).contentHash();
    }

    private void stubSend(FaxSendResult result) {
        given(faxMessageRepository.findMaxSequence(10L)).willReturn(0);
        given(faxMessageRepository.saveIfAbsent(any())).willAnswer(inv -> Optional.of(inv.getArgument(0)));
        given(accountResolver.resolve()).willReturn(Optional.of(ACCOUNT));
        given(renderer.renderPdf(any())).willReturn(new byte[]{1, 2, 3});
        given(gateway.send(any(), anyString(), any(), anyString(), anyString(), anyString())).willReturn(result);
        given(faxMessageRepository.findById(any())).willReturn(Optional.empty());
        given(faxMessageRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 수주완료_주문은_수주사에게_팩스를_발송한다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        given(faxMessageRepository.findLatestEffectiveByOrderId(10L)).willReturn(Optional.empty());
        stubSend(new FaxSendResult(true, "SK-1", null));

        service.dispatchIfNeeded(10L);

        ArgumentCaptor<String> to = ArgumentCaptor.forClass(String.class);
        verify(gateway).send(eq(ACCOUNT), anyString(), any(), to.capture(), anyString(), anyString());
        assertThat(to.getValue()).isEqualTo("021112222");
        ArgumentCaptor<FaxMessage> saved = ArgumentCaptor.forClass(FaxMessage.class);
        verify(faxMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(FaxStatus.SUBMITTED);
        assertThat(saved.getValue().getSendKey()).isEqualTo("SK-1");
    }

    @Test
    void 같은_수주사_같은_내용이면_다시_발송하지_않는다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        FaxMessage last = FaxMessage.of(1L, 10L, 1, 100L, "021112222", hashOf(order), FaxStatus.DELIVERED, "SK", null,
                false, LocalDateTime.now(), LocalDateTime.now());
        given(faxMessageRepository.findLatestEffectiveByOrderId(10L)).willReturn(Optional.of(last));

        service.dispatchIfNeeded(10L);

        verify(faxMessageRepository, never()).saveIfAbsent(any());
        verify(gateway, never()).send(any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void 주문서_내용이_바뀌면_다시_발송한다() {
        Order order = order(OrderStatus.ACCEPTED, "부산");
        stubEnvironment(order);
        String oldHash = hashOf(order(OrderStatus.ACCEPTED, "서울"));
        FaxMessage last = FaxMessage.of(1L, 10L, 1, 100L, "021112222", oldHash, FaxStatus.SUBMITTED, "SK", null,
                false, LocalDateTime.now(), null);
        given(faxMessageRepository.findLatestEffectiveByOrderId(10L)).willReturn(Optional.of(last));
        stubSend(new FaxSendResult(true, "SK-2", null));
        given(faxMessageRepository.findMaxSequence(10L)).willReturn(1);

        service.dispatchIfNeeded(10L);

        verify(gateway).send(any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void 이전_수주사에게_보낸_기록만_있으면_새_수주사에게_발송한다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        FaxMessage previousVendor = FaxMessage.of(1L, 10L, 1, 999L, "029998888", hashOf(order), FaxStatus.DELIVERED, "SK", null,
                false, LocalDateTime.now(), LocalDateTime.now());
        given(faxMessageRepository.findLatestEffectiveByOrderId(10L)).willReturn(Optional.of(previousVendor));
        stubSend(new FaxSendResult(true, "SK-3", null));
        given(faxMessageRepository.findMaxSequence(10L)).willReturn(1);

        service.dispatchIfNeeded(10L);

        verify(gateway).send(any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void 발송에_실패하면_실패로_기록한다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        given(faxMessageRepository.findLatestEffectiveByOrderId(10L)).willReturn(Optional.empty());
        stubSend(new FaxSendResult(false, null, "바로빌 오류 (-1)"));

        service.dispatchIfNeeded(10L);

        ArgumentCaptor<FaxMessage> saved = ArgumentCaptor.forClass(FaxMessage.class);
        verify(faxMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(FaxStatus.FAILED);
        assertThat(saved.getValue().getFailReason()).contains("-1");
    }

    @Test
    void 수주완료가_아니면_발송하지_않는다() {
        given(orderRepository.findById(10L)).willReturn(Optional.of(order(OrderStatus.REJECTED, "서울")));

        service.dispatchIfNeeded(10L);

        verify(faxMessageRepository, never()).saveIfAbsent(any());
    }

    @Test
    void 팩스_미연동_수주사에는_발송하지_않는다() {
        given(orderRepository.findById(10L)).willReturn(Optional.of(order(OrderStatus.ACCEPTED, "서울")));
        given(companyRepository.findById(100L)).willReturn(Optional.of(company(100L, false, "02-111-2222")));

        service.dispatchIfNeeded(10L);

        verify(faxMessageRepository, never()).saveIfAbsent(any());
    }

    @Test
    void 동시_요청으로_순번이_이미_있으면_발송하지_않는다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        given(faxMessageRepository.findLatestEffectiveByOrderId(10L)).willReturn(Optional.empty());
        given(faxMessageRepository.findMaxSequence(10L)).willReturn(0);
        given(faxMessageRepository.saveIfAbsent(any())).willReturn(Optional.empty());

        service.dispatchIfNeeded(10L);

        verify(gateway, never()).send(any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void 연동_설정이_없으면_실패로_기록하고_발송하지_않는다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        given(faxMessageRepository.findLatestEffectiveByOrderId(10L)).willReturn(Optional.empty());
        given(faxMessageRepository.findMaxSequence(10L)).willReturn(0);
        given(faxMessageRepository.saveIfAbsent(any())).willAnswer(inv -> Optional.of(inv.getArgument(0)));
        given(accountResolver.resolve()).willReturn(Optional.empty());
        given(faxMessageRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.dispatchIfNeeded(10L);

        ArgumentCaptor<FaxMessage> saved = ArgumentCaptor.forClass(FaxMessage.class);
        verify(faxMessageRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(FaxStatus.FAILED);
        verify(gateway, never()).send(any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void 수동_재발송은_중복_검사를_건너뛴다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        stubSend(new FaxSendResult(true, "SK-9", null));
        given(faxMessageRepository.findMaxSequence(10L)).willReturn(2);

        FaxMessageResult result = service.resend(10L);

        assertThat(result.manual()).isTrue();
        assertThat(result.status()).isEqualTo("SUBMITTED");
        verify(faxMessageRepository, never()).findLatestEffectiveByOrderId(any());
    }

    @Test
    void 수동_재발송은_수주완료_또는_배송완료_주문만_가능하다() {
        given(orderRepository.findById(10L)).willReturn(Optional.of(order(OrderStatus.PENDING, "서울")));

        assertThatThrownBy(() -> service.resend(10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(FaxErrorCode.FAX_NOT_ALLOWED));
    }

    @Test
    void 수동_재발송이_동시에_들어오면_진행중_오류로_응답한다() {
        Order order = order(OrderStatus.ACCEPTED, "서울");
        stubEnvironment(order);
        given(faxMessageRepository.findMaxSequence(10L)).willReturn(0);
        given(faxMessageRepository.saveIfAbsent(any())).willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.resend(10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(FaxErrorCode.FAX_IN_PROGRESS));
    }
}
