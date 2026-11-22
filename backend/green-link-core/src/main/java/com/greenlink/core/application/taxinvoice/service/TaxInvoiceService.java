package com.greenlink.core.application.taxinvoice.service;

import com.greenlink.core.application.taxinvoice.dto.TaxInvoicePreviewResult;
import com.greenlink.core.application.taxinvoice.dto.TaxInvoicePreviewResult.PartyInfo;
import com.greenlink.core.application.taxinvoice.dto.TaxInvoiceResult;
import com.greenlink.core.application.taxinvoice.usecase.GetTaxInvoiceStatusUseCase;
import com.greenlink.core.application.taxinvoice.usecase.IssueTaxInvoiceUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.model.PointTransactionType;
import com.greenlink.core.domain.point.port.PointTransactionRepository;
import com.greenlink.core.domain.taxinvoice.model.TaxInvoice;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Account;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.IssueRequest;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.IssueResult;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Party;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.taxinvoice.TaxInvoiceErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TaxInvoiceService implements IssueTaxInvoiceUseCase, GetTaxInvoiceStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(TaxInvoiceService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String ITEM_NAME = "포인트 충전";
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final PointTransactionRepository pointTransactionRepository;
    private final CompanyRepository companyRepository;
    private final TaxInvoiceRepository taxInvoiceRepository;
    private final TaxInvoiceAccountResolver accountResolver;
    private final TaxInvoiceGateway gateway;
    private final TransactionTemplate tx;

    public TaxInvoiceService(PointTransactionRepository pointTransactionRepository,
                             CompanyRepository companyRepository,
                             TaxInvoiceRepository taxInvoiceRepository,
                             TaxInvoiceAccountResolver accountResolver,
                             TaxInvoiceGateway gateway,
                             PlatformTransactionManager transactionManager) {
        this.pointTransactionRepository = pointTransactionRepository;
        this.companyRepository = companyRepository;
        this.taxInvoiceRepository = taxInvoiceRepository;
        this.accountResolver = accountResolver;
        this.gateway = gateway;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxInvoicePreviewResult preview(Long pointTransactionId) {
        PointTransaction pt = loadChargeTransaction(pointTransactionId);
        Company company = loadCompany(pt);
        Optional<TaxInvoice> existing = taxInvoiceRepository.findByPointTransactionId(pointTransactionId);
        String email = existing.map(TaxInvoice::getRecipientEmail).orElse(company.getTaxInvoiceEmail());
        Party recipient = recipientParty(company, email);
        Optional<Account> account = accountResolver.resolve();
        Party supplier = account.map(accountResolver::supplier).orElse(new Party(null, null, null, null, null));

        List<String> missing = new ArrayList<>();
        if (account.isEmpty()) missing.add("바로빌 계정 설정(연동인증키·아이디·사업자번호)");
        if (blank(recipient.corpNum()) || recipient.corpNum().length() != 10) missing.add("발주사 사업자번호");
        if (blank(recipient.ceoName())) missing.add("발주사 대표자명");
        if (blank(recipient.address())) missing.add("발주사 주소");

        return new TaxInvoicePreviewResult(
                pointTransactionId, ITEM_NAME, pt.getAmount(), 0L, pt.getAmount(),
                toInfo(supplier), toInfo(recipient),
                existing.map(t -> t.getStatus().name()).orElse(null),
                existing.map(TaxInvoice::getFailReason).orElse(null),
                missing);
    }

    @Override
    public TaxInvoiceResult issue(Long pointTransactionId, String recipientEmail, Long adminId) {
        Pending pending;
        try {
            pending = tx.execute(status -> prepare(pointTransactionId, recipientEmail, adminId));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(TaxInvoiceErrorCode.ISSUE_IN_PROGRESS);
        }
        return execute(pending);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, TaxInvoiceResult> getByPointTransactionIds(Collection<Long> pointTransactionIds) {
        if (pointTransactionIds == null || pointTransactionIds.isEmpty()) return Map.of();
        return taxInvoiceRepository.findByPointTransactionIds(pointTransactionIds).stream()
                .collect(Collectors.toMap(TaxInvoice::getPointTransactionId, TaxInvoiceResult::from, (a, b) -> a));
    }

    private Pending prepare(Long pointTransactionId, String rawEmail, Long adminId) {
        PointTransaction pt = loadChargeTransaction(pointTransactionId);
        Company company = loadCompany(pt);
        String email = resolveEmail(rawEmail, company);
        Party recipient = recipientParty(company, email);
        if (blank(recipient.corpNum()) || recipient.corpNum().length() != 10
                || blank(recipient.companyName()) || blank(recipient.ceoName()) || blank(recipient.address())) {
            throw new BusinessException(TaxInvoiceErrorCode.RECIPIENT_INFO_MISSING);
        }
        Account account = accountResolver.resolve()
                .orElseThrow(() -> new BusinessException(TaxInvoiceErrorCode.SUPPLIER_NOT_CONFIGURED));
        Party supplier = accountResolver.supplier(account);
        LocalDateTime now = now();

        TaxInvoice invoice;
        boolean reconcile = false;
        Optional<TaxInvoice> existing = taxInvoiceRepository.findByPointTransactionIdForUpdate(pointTransactionId);
        if (existing.isPresent()) {
            invoice = existing.get();
            switch (invoice.getStatus()) {
                case SUCCESS -> throw new BusinessException(TaxInvoiceErrorCode.ALREADY_ISSUED);
                case ISSUING -> {
                    if (!invoice.isStaleIssuing(now)) {
                        throw new BusinessException(TaxInvoiceErrorCode.ISSUE_IN_PROGRESS);
                    }
                    reconcile = true;
                }
                case FAILED -> {
                    invoice.restart(email, adminId, now);
                    invoice = taxInvoiceRepository.save(invoice);
                }
            }
        } else {
            TaxInvoice started = TaxInvoice.start(pointTransactionId, company.getId(), pt.getAmount(), email, adminId, now);
            invoice = taxInvoiceRepository.saveIfAbsent(started)
                    .orElseThrow(() -> new BusinessException(TaxInvoiceErrorCode.ISSUE_IN_PROGRESS));
        }

        if (!reconcile && !email.equals(company.getTaxInvoiceEmail())) {
            company.changeTaxInvoiceEmail(email);
            companyRepository.save(company);
        }
        IssueRequest request = new IssueRequest(supplier, recipient, invoice.getMgtNum(),
                LocalDate.now(KST), ITEM_NAME, invoice.getSupplyAmount());
        return new Pending(invoice, account, request, reconcile);
    }

    private TaxInvoiceResult execute(Pending p) {
        IssueResult result;
        try {
            result = p.reconcile() ? reconcile(p) : gateway.issue(p.account(), p.request());
        } catch (Exception e) {
            // 호출 결과를 알 수 없는 경우(타임아웃 등)에는 실패로 단정하지 않고 발행 중으로 둔다. 중복 발행을 막기 위함.
            log.warn("[전자계산서 발행 결과 미확인] txId={} error={}", p.invoice().getPointTransactionId(), e.getClass().getSimpleName());
            return TaxInvoiceResult.from(p.invoice());
        }
        IssueResult finalResult = result;
        TaxInvoice completed = tx.execute(status -> {
            TaxInvoice t = taxInvoiceRepository.findById(p.invoice().getId()).orElse(p.invoice());
            if (finalResult.success()) {
                t.markSuccess(finalResult.docKey(), now());
            } else {
                t.markFailed(finalResult.failReason());
            }
            return taxInvoiceRepository.save(t);
        });
        return TaxInvoiceResult.from(completed);
    }

    private IssueResult reconcile(Pending p) {
        Optional<String> docKey = gateway.findIssuedDocKey(p.account(), p.invoice().getMgtNum());
        return docKey.map(k -> new IssueResult(true, k, null))
                .orElseGet(() -> new IssueResult(false, null, "이전 발행 요청의 결과를 확인하지 못했습니다. 다시 시도해 주세요."));
    }

    private PointTransaction loadChargeTransaction(Long id) {
        PointTransaction pt = pointTransactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(TaxInvoiceErrorCode.TRANSACTION_NOT_FOUND));
        if (pt.getType() != PointTransactionType.CHARGE || pt.getAmount() <= 0) {
            throw new BusinessException(TaxInvoiceErrorCode.NOT_CHARGE_TRANSACTION);
        }
        return pt;
    }

    private Company loadCompany(PointTransaction pt) {
        return companyRepository.findById(pt.getCompanyId())
                .orElseThrow(() -> new BusinessException(TaxInvoiceErrorCode.TRANSACTION_NOT_FOUND));
    }

    private String resolveEmail(String raw, Company company) {
        String email = blank(raw) ? company.getTaxInvoiceEmail() : raw.trim();
        if (blank(email) || email.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(email).matches()) {
            throw new BusinessException(TaxInvoiceErrorCode.EMAIL_INVALID);
        }
        return email;
    }

    private Party recipientParty(Company c, String email) {
        return new Party(TaxInvoiceAccountResolver.digits(c.getBusinessNo()), c.getCompanyName(), c.getCeoName(),
                TaxInvoiceAccountResolver.join(c.getAddress(), c.getDetailAddress()), email);
    }

    private PartyInfo toInfo(Party p) {
        return new PartyInfo(p.corpNum(), p.companyName(), p.ceoName(), p.address(), p.email());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(KST);
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private record Pending(TaxInvoice invoice, Account account, IssueRequest request, boolean reconcile) {}
}
