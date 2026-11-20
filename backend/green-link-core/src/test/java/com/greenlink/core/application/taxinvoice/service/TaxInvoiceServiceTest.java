package com.greenlink.core.application.taxinvoice.service;

import com.greenlink.core.application.taxinvoice.dto.TaxInvoicePreviewResult;
import com.greenlink.core.application.taxinvoice.dto.TaxInvoiceResult;
import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.model.PointTransactionType;
import com.greenlink.core.domain.point.port.PointTransactionRepository;
import com.greenlink.core.domain.taxinvoice.model.TaxInvoice;
import com.greenlink.core.domain.taxinvoice.model.TaxInvoiceStatus;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Account;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.IssueResult;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Party;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.taxinvoice.TaxInvoiceErrorCode;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaxInvoiceServiceTest {

    @Mock PointTransactionRepository pointTransactionRepository;
    @Mock CompanyRepository companyRepository;
    @Mock TaxInvoiceRepository taxInvoiceRepository;
    @Mock TaxInvoiceAccountResolver accountResolver;
    @Mock TaxInvoiceGateway gateway;
    @Mock PlatformTransactionManager transactionManager;

    private TaxInvoiceService service;

    private static final Account ACCOUNT = new Account("key", "1234567890", "baro");
    private static final Party SUPPLIER = new Party("1234567890", "온우림", "대표", "서울", null);

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new TaxInvoiceService(pointTransactionRepository, companyRepository, taxInvoiceRepository,
                accountResolver, gateway, transactionManager);
    }

    private PointTransaction charge(PointTransactionType type, long amount) {
        return PointTransaction.of(5L, 100L, null, 1L, null, type, amount, amount, "충전", "2026-11-21T10:00:00");
    }

    private Company company(String businessNo, String ceo, String address, String email) {
        Company c = Company.of(100L, businessNo, "발주사", ceo, "02-000-0000", null, address, "3층",
                CompanyType.ORDERING, CompanyStatus.ACTIVE, true, null, null, null, null, null, BusinessStatus.OPEN, 3000L);
        c.restoreTaxInvoiceEmail(email);
        return c;
    }

    private void stubReady(Company company) {
        given(pointTransactionRepository.findById(5L)).willReturn(Optional.of(charge(PointTransactionType.CHARGE, 100000L)));
        given(companyRepository.findById(100L)).willReturn(Optional.of(company));
    }

    private void stubAccount() {
        given(accountResolver.resolve()).willReturn(Optional.of(ACCOUNT));
        given(accountResolver.supplier(ACCOUNT)).willReturn(SUPPLIER);
    }

    private void stubSave() {
        lenient().when(taxInvoiceRepository.saveIfAbsent(any())).thenAnswer(inv -> Optional.of(inv.getArgument(0)));
        lenient().when(taxInvoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(taxInvoiceRepository.findById(any())).thenReturn(Optional.empty());
    }

    private TaxInvoice existing(TaxInvoiceStatus status, LocalDateTime requestedAt) {
        return TaxInvoice.of(1L, 5L, 100L, status, 100000L, 0L, 1, "GL-5-1", null, "실패", "a@b.com", 1L, requestedAt, null);
    }

    @Test
    void 충전_거래를_면세로_발행하고_성공을_기록한다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", null));
        stubAccount();
        stubSave();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L)).willReturn(Optional.empty());
        given(gateway.issue(eq(ACCOUNT), any())).willReturn(new IssueResult(true, "GL-5-1", null));

        TaxInvoiceResult result = service.issue(5L, "billing@test.com", 1L);

        assertThat(result.status()).isEqualTo("SUCCESS");
        ArgumentCaptor<TaxInvoiceGateway.IssueRequest> req = ArgumentCaptor.forClass(TaxInvoiceGateway.IssueRequest.class);
        verify(gateway).issue(eq(ACCOUNT), req.capture());
        assertThat(req.getValue().supplyAmount()).isEqualTo(100000L);
        assertThat(req.getValue().mgtNum()).isEqualTo("GL-5-1");
        assertThat(req.getValue().recipient().corpNum()).isEqualTo("1234567890");
        assertThat(req.getValue().recipient().email()).isEqualTo("billing@test.com");
        ArgumentCaptor<Company> savedCompany = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(savedCompany.capture());
        assertThat(savedCompany.getValue().getTaxInvoiceEmail()).isEqualTo("billing@test.com");
    }

    @Test
    void 바로빌이_실패를_반환하면_실패로_기록한다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", null));
        stubAccount();
        stubSave();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L)).willReturn(Optional.empty());
        given(gateway.issue(any(), any())).willReturn(new IssueResult(false, null, "바로빌 오류 (-11010)"));

        TaxInvoiceResult result = service.issue(5L, "billing@test.com", 1L);

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.failReason()).contains("-11010");
    }

    @Test
    void 호출_결과를_알_수_없으면_발행중으로_남긴다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", null));
        stubAccount();
        stubSave();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L)).willReturn(Optional.empty());
        given(gateway.issue(any(), any())).willThrow(new IllegalStateException("timeout"));

        TaxInvoiceResult result = service.issue(5L, "billing@test.com", 1L);

        assertThat(result.status()).isEqualTo("ISSUING");
        verify(taxInvoiceRepository, never()).save(any());
    }

    @Test
    void 충전이_아닌_거래는_거부한다() {
        given(pointTransactionRepository.findById(5L)).willReturn(Optional.of(charge(PointTransactionType.USE, 1000L)));

        assertThatThrownBy(() -> service.issue(5L, "a@b.com", 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(TaxInvoiceErrorCode.NOT_CHARGE_TRANSACTION));
    }

    @Test
    void 이미_발행된_건은_다시_발행하지_않는다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", "a@b.com"));
        stubAccount();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L))
                .willReturn(Optional.of(existing(TaxInvoiceStatus.SUCCESS, LocalDateTime.now())));

        assertThatThrownBy(() -> service.issue(5L, "a@b.com", 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(TaxInvoiceErrorCode.ALREADY_ISSUED));
        verify(gateway, never()).issue(any(), any());
    }

    @Test
    void 방금_시작한_발행중_건은_409이다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", "a@b.com"));
        stubAccount();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L))
                .willReturn(Optional.of(existing(TaxInvoiceStatus.ISSUING, LocalDateTime.now())));

        assertThatThrownBy(() -> service.issue(5L, "a@b.com", 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(TaxInvoiceErrorCode.ISSUE_IN_PROGRESS));
    }

    @Test
    void 오래된_발행중_건은_바로빌_조회로_복구한다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", "a@b.com"));
        stubAccount();
        stubSave();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L))
                .willReturn(Optional.of(existing(TaxInvoiceStatus.ISSUING, LocalDateTime.now().minusMinutes(30))));
        given(gateway.findIssuedDocKey(ACCOUNT, "GL-5-1")).willReturn(Optional.of("GL-5-1"));

        TaxInvoiceResult result = service.issue(5L, "a@b.com", 1L);

        assertThat(result.status()).isEqualTo("SUCCESS");
        verify(gateway, never()).issue(any(), any());
    }

    @Test
    void 실패한_건은_새_관리번호로_재시도한다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", "a@b.com"));
        stubAccount();
        stubSave();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L))
                .willReturn(Optional.of(existing(TaxInvoiceStatus.FAILED, LocalDateTime.now().minusHours(1))));
        given(gateway.issue(any(), any())).willReturn(new IssueResult(true, "GL-5-2", null));

        TaxInvoiceResult result = service.issue(5L, "a@b.com", 1L);

        assertThat(result.status()).isEqualTo("SUCCESS");
        ArgumentCaptor<TaxInvoiceGateway.IssueRequest> req = ArgumentCaptor.forClass(TaxInvoiceGateway.IssueRequest.class);
        verify(gateway).issue(any(), req.capture());
        assertThat(req.getValue().mgtNum()).isEqualTo("GL-5-2");
    }

    @Test
    void 발주사_사업자정보가_없으면_거부한다() {
        stubReady(company(null, null, null, "a@b.com"));

        assertThatThrownBy(() -> service.issue(5L, "a@b.com", 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(TaxInvoiceErrorCode.RECIPIENT_INFO_MISSING));
    }

    @Test
    void 이메일_형식이_잘못되면_거부한다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", null));

        assertThatThrownBy(() -> service.issue(5L, "not-an-email", 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(TaxInvoiceErrorCode.EMAIL_INVALID));
    }

    @Test
    void 바로빌_계정이_없으면_거부한다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", "a@b.com"));
        given(accountResolver.resolve()).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.issue(5L, "a@b.com", 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(TaxInvoiceErrorCode.SUPPLIER_NOT_CONFIGURED));
    }

    @Test
    void 동시_발행으로_유니크_제약이_충돌하면_409이다() {
        stubReady(company("123-45-67890", "홍길동", "서울시", "a@b.com"));
        stubAccount();
        given(taxInvoiceRepository.findByPointTransactionIdForUpdate(5L)).willReturn(Optional.empty());
        given(taxInvoiceRepository.saveIfAbsent(any())).willThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> service.issue(5L, "a@b.com", 1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(TaxInvoiceErrorCode.ISSUE_IN_PROGRESS));
    }

    @Test
    void 미리보기는_면세_세액0과_누락항목을_알려준다() {
        stubReady(company("123-45-67890", null, "서울시", "a@b.com"));
        given(taxInvoiceRepository.findByPointTransactionId(5L)).willReturn(Optional.empty());
        given(accountResolver.resolve()).willReturn(Optional.empty());

        TaxInvoicePreviewResult preview = service.preview(5L);

        assertThat(preview.supplyAmount()).isEqualTo(100000L);
        assertThat(preview.taxAmount()).isZero();
        assertThat(preview.totalAmount()).isEqualTo(100000L);
        assertThat(preview.missing()).anyMatch(m -> m.contains("바로빌")).anyMatch(m -> m.contains("대표자"));
    }
}
