package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.common.BusinessNumber;
import com.greenlink.core.application.sms.usecase.SendDeliveryAlimtalkUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.AlimtalkRequest;
import com.greenlink.core.domain.sms.model.AlimtalkRequest.AlimtalkVariables;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.port.AlimtalkClient;
import com.greenlink.core.domain.sms.port.AlimtalkClientFactory;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlimtalkService implements SendDeliveryAlimtalkUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlimtalkService.class);

    private final OrderRepository orderRepository;
    private final CompanyIntegrationRepository companyIntegrationRepository;
    private final CompanyRepository companyRepository;
    private final AlimtalkClientFactory alimtalkClientFactory;
    private final SmsMessageRepository smsMessageRepository;

    public AlimtalkService(OrderRepository orderRepository,
                           CompanyIntegrationRepository companyIntegrationRepository,
                           CompanyRepository companyRepository,
                           AlimtalkClientFactory alimtalkClientFactory,
                           SmsMessageRepository smsMessageRepository) {
        this.orderRepository = orderRepository;
        this.companyIntegrationRepository = companyIntegrationRepository;
        this.companyRepository = companyRepository;
        this.alimtalkClientFactory = alimtalkClientFactory;
        this.smsMessageRepository = smsMessageRepository;
    }

    @Override
    public void send(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderingCompanyId() == null) return;

        CompanyIntegration ci = companyIntegrationRepository
                .findByCompanyId(order.getOrderingCompanyId())
                .orElse(null);

        if (ci == null || !ci.isAlimtalkEnabled()) return;
        if (ci.getApiKey() == null || ci.getApiKey().isBlank()) return;
        if (ci.getAlimtalkSenderKey() == null || ci.getAlimtalkSenderKey().isBlank()) return;

        String templateCode = ci.resolveAlimtalkTemplate(order.getEventType());
        if (templateCode == null || templateCode.isBlank()) return;

        AlimtalkVariables variables = buildVariables(order);
        AlimtalkClient client = alimtalkClientFactory.getByProvider(ci.getProvider());
        String corpNum = isBarobill(ci) ? companyBusinessNo(order.getOrderingCompanyId()) : null;
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        for (String receiver : resolveReceivers(order)) {
            SmsMessage msg = SmsMessage.createAlimtalk(orderId, receiver, templateCode);
            msg.setCompanyId(order.getOrderingCompanyId());
            if (isBarobill(ci) && (corpNum == null || corpNum.isBlank())) {
                msg.markFail("회사 정보에 사업자번호가 없습니다.", now);
                smsMessageRepository.save(msg);
                continue;
            }
            try {
                AlimtalkClient.AlimtalkSendResult result = client.send(new AlimtalkRequest(
                        ci.getApiKey(), ci.getUserId(), corpNum, ci.getAlimtalkSenderKey(),
                        templateCode, ci.getSenderNumber(), receiver, variables));
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

    private AlimtalkVariables buildVariables(Order order) {
        String orderingCompanyName = "";
        if (order.getOrderingCompanyId() != null) {
            orderingCompanyName = companyRepository.findById(order.getOrderingCompanyId())
                    .map(Company::getCompanyName).orElse("");
        }
        String recipientName = order.getRecipientName() != null && !order.getRecipientName().isBlank()
                ? order.getRecipientName() : order.getOrdererName();
        return new AlimtalkVariables(orderingCompanyName, recipientName, order.getOrderNumber(),
                order.getPlaceName(), order.getPhotoToken());
    }

    private boolean isBarobill(CompanyIntegration ci) {
        return "BAROBILL".equalsIgnoreCase(ci.getProvider());
    }

    private String companyBusinessNo(Long companyId) {
        return companyRepository.findById(companyId)
                .map(c -> BusinessNumber.normalize(c.getBusinessNo()))
                .orElse(null);
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
