package com.greenlink.core.application.fax.service;

import com.greenlink.core.application.fax.dto.FaxMessageResult;
import com.greenlink.core.application.fax.usecase.DispatchOrderFaxUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.fax.model.FaxMessage;
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
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class OrderFaxService implements DispatchOrderFaxUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderFaxService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CompanyRepository companyRepository;
    private final FaxMessageRepository faxMessageRepository;
    private final FaxAccountResolver accountResolver;
    private final OrderSheetRenderer renderer;
    private final FaxGateway gateway;
    private final TransactionTemplate tx;

    public OrderFaxService(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           CompanyRepository companyRepository,
                           FaxMessageRepository faxMessageRepository,
                           FaxAccountResolver accountResolver,
                           OrderSheetRenderer renderer,
                           FaxGateway gateway,
                           PlatformTransactionManager transactionManager) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.companyRepository = companyRepository;
        this.faxMessageRepository = faxMessageRepository;
        this.accountResolver = accountResolver;
        this.renderer = renderer;
        this.gateway = gateway;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void dispatchIfNeeded(Long orderId) {
        try {
            run(orderId, false);
        } catch (Exception e) {
            log.warn("[팩스 자동발송 실패] orderId={} error={}", orderId, e.getClass().getSimpleName());
        }
    }

    @Override
    public FaxMessageResult resend(Long orderId) {
        Pending pending;
        try {
            pending = tx.execute(status -> prepare(orderId, true));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(FaxErrorCode.FAX_IN_PROGRESS);
        }
        if (pending == null) {
            throw new BusinessException(FaxErrorCode.FAX_NOT_CONFIGURED);
        }
        return execute(pending);
    }

    private void run(Long orderId, boolean manual) {
        Pending pending = tx.execute(status -> prepare(orderId, manual));
        if (pending != null) {
            execute(pending);
        }
    }

    private Pending prepare(Long orderId, boolean manual) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.isDeleted()) {
            if (manual) throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
            return null;
        }
        boolean allowed = manual
                ? order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.DELIVERED
                : order.getStatus() == OrderStatus.ACCEPTED;
        if (!allowed) {
            if (manual) throw new BusinessException(FaxErrorCode.FAX_NOT_ALLOWED);
            return null;
        }
        Company vendor = order.getVendorCompanyId() == null ? null : companyRepository.findById(order.getVendorCompanyId()).orElse(null);
        if (vendor == null || !vendor.canReceiveFax()) {
            if (manual) throw new BusinessException(FaxErrorCode.FAX_VENDOR_NOT_ENABLED);
            return null;
        }
        Company ordering = order.getOrderingCompanyId() == null ? null : companyRepository.findById(order.getOrderingCompanyId()).orElse(null);
        List<OrderItem> items = loadItems(order);
        OrderSheet sheet = OrderSheetFactory.create(order, items, ordering, vendor);
        String hash = sheet.contentHash();

        if (!manual) {
            Optional<FaxMessage> last = faxMessageRepository.findLatestEffectiveByOrderId(orderId);
            if (last.isPresent()
                    && vendor.getId().equals(last.get().getVendorCompanyId())
                    && hash.equals(last.get().getContentHash())) {
                return null;
            }
        }

        String toNumber = vendor.getFaxNumber().replaceAll("[^0-9]", "");
        int sequence = faxMessageRepository.findMaxSequence(orderId) + 1;
        FaxMessage started = FaxMessage.start(orderId, sequence, vendor.getId(), toNumber, hash, manual, now());
        Optional<FaxMessage> saved = faxMessageRepository.saveIfAbsent(started);
        if (saved.isEmpty()) {
            return null;
        }

        Optional<FaxAccount> account = accountResolver.resolve();
        if (account.isEmpty()) {
            FaxMessage message = saved.get();
            message.markFailed("팩스 연동 설정이 완료되지 않았습니다.", now());
            faxMessageRepository.save(message);
            if (manual) throw new BusinessException(FaxErrorCode.FAX_NOT_CONFIGURED);
            return null;
        }
        return new Pending(saved.get(), sheet, account.get(), vendor.getCompanyName(), order.getOrderNumber());
    }

    private FaxMessageResult execute(Pending p) {
        FaxSendResult result;
        try {
            byte[] pdf = renderer.renderPdf(p.sheet());
            String fileName = fileName(p.message());
            result = gateway.send(p.account(), fileName, pdf, p.message().getToNumber(), p.vendorName(),
                    "fax-" + p.message().getOrderId() + "-" + p.message().getSequence());
        } catch (Exception e) {
            log.warn("[팩스 발송 실패] orderId={} error={}", p.message().getOrderId(), e.getClass().getSimpleName());
            result = new FaxSendResult(false, null, "팩스 발송 처리 중 오류: " + e.getClass().getSimpleName());
        }
        FaxSendResult finalResult = result;
        FaxMessage completed = tx.execute(status -> {
            FaxMessage m = faxMessageRepository.findById(p.message().getId()).orElse(p.message());
            if (finalResult.success()) {
                m.markSubmitted(finalResult.sendKey());
            } else {
                m.markFailed(finalResult.failReason(), now());
            }
            return faxMessageRepository.save(m);
        });
        return FaxMessageResult.of(completed, p.orderNumber(), p.vendorName());
    }

    private List<OrderItem> loadItems(Order order) {
        List<OrderItem> items = orderItemRepository.findAllByOrderId(order.getId());
        if (items != null && !items.isEmpty()) return items;
        return List.of(OrderItem.fromLegacy(order.getId(), order.getProductId(), order.getProductName(),
                order.getQuantity(), order.getAmount()));
    }

    private String fileName(FaxMessage m) {
        return "order-" + m.getOrderId() + "-" + m.getSequence() + ".pdf";
    }

    private LocalDateTime now() {
        return LocalDateTime.now(KST);
    }

    private record Pending(FaxMessage message, OrderSheet sheet, FaxAccount account, String vendorName, String orderNumber) {}
}
