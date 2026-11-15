package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.sms.usecase.SendDeliveryAlimtalkUseCase;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.port.AlimtalkClient;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlimtalkService implements SendDeliveryAlimtalkUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlimtalkService.class);

    private final OrderRepository orderRepository;
    private final CompanyIntegrationRepository companyIntegrationRepository;
    private final AlimtalkClient alimtalkClient;
    private final SmsMessageRepository smsMessageRepository;

    public AlimtalkService(OrderRepository orderRepository,
                           CompanyIntegrationRepository companyIntegrationRepository,
                           AlimtalkClient alimtalkClient,
                           SmsMessageRepository smsMessageRepository) {
        this.orderRepository = orderRepository;
        this.companyIntegrationRepository = companyIntegrationRepository;
        this.alimtalkClient = alimtalkClient;
        this.smsMessageRepository = smsMessageRepository;
    }

    @Override
    @Transactional
    public void send(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderingCompanyId() == null) return;

        CompanyIntegration ci = companyIntegrationRepository
                .findByCompanyId(order.getOrderingCompanyId())
                .orElse(null);

        if (ci == null || !ci.isAlimtalkEnabled()) return;
        if (ci.getApiKey() == null || ci.getApiKey().isBlank()) return;

        String templateCode = ci.resolveAlimtalkTemplate(order.getEventType());
        if (templateCode == null || templateCode.isBlank()) return;

        String variable = buildVariable(order);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        for (String receiver : resolveReceivers(order)) {
            SmsMessage msg = SmsMessage.createAlimtalk(orderId, receiver, templateCode);
            try {
                AlimtalkClient.AlimtalkSendResult result = alimtalkClient.send(
                        ci.getApiKey(), templateCode, variable, ci.getSenderNumber(), receiver);
                if (result.success()) {
                    msg.markAlimtalkSuccess(now, result.replaced());
                } else {
                    msg.markFail(result.resultCode(), now);
                }
            } catch (Exception e) {
                log.error("[알림톡 발송 실패] orderId={} receiver={} error={}", orderId, receiver, e.getMessage(), e);
                msg.markFail(e.getMessage(), now);
            }
            smsMessageRepository.save(msg);
        }
    }

    private String buildVariable(Order order) {
        String recipient = sanitize(order.getRecipientName() != null && !order.getRecipientName().isBlank()
                ? order.getRecipientName() : order.getOrdererName());
        String orderNumber = sanitize(order.getOrderNumber());
        String placeName = sanitize(order.getPlaceName() != null ? order.getPlaceName() : "");
        String photoToken = sanitize(order.getPhotoToken() != null ? order.getPhotoToken() : "");
        return String.join("|", recipient, orderNumber, placeName, photoToken);
    }

    private String sanitize(String val) {
        return val != null ? val.replace("|", "") : "";
    }

    private List<String> resolveReceivers(Order order) {
        List<String> receivers = new ArrayList<>();
        if (order.isSendPhotoToOrderer() && isValidPhone(order.getOrdererPhone())) {
            receivers.add(order.getOrdererPhone());
        }
        if (order.isSendPhotoToRecipient() && isValidPhone(order.getRecipientPhone())
                && !order.getRecipientPhone().equals(order.getOrdererPhone())) {
            receivers.add(order.getRecipientPhone());
        }
        return receivers;
    }

    private boolean isValidPhone(String phone) {
        return phone != null && !phone.isBlank();
    }
}
