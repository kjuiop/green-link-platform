package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.sms.dto.SmsMessageResult;
import com.greenlink.core.application.sms.dto.SmsResult;
import com.greenlink.core.application.sms.usecase.GetSmsMessagesUseCase;
import com.greenlink.core.application.sms.usecase.ResendMmsUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryMmsUseCase;
import com.greenlink.core.application.sms.usecase.SendOrderNotificationUseCase;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class SmsService implements SendDeliveryMmsUseCase, ResendMmsUseCase, GetSmsMessagesUseCase, SendOrderNotificationUseCase {

    private final OrderRepository orderRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsClient smsClient;
    private final SmsSettingsRepository smsSettingsRepository;

    @Value("${app.portal-base-url:}")
    private String portalBaseUrl;

    public SmsService(OrderRepository orderRepository,
                      SmsMessageRepository smsMessageRepository,
                      SmsClient smsClient,
                      SmsSettingsRepository smsSettingsRepository) {
        this.orderRepository = orderRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.smsClient = smsClient;
        this.smsSettingsRepository = smsSettingsRepository;
    }

    @Override
    @Transactional
    public void send(Long orderId, List<String> photoUrls) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        List<String> urls = (photoUrls != null && !photoUrls.isEmpty())
                ? photoUrls
                : Collections.singletonList(null);

        SmsSettings settings = smsSettingsRepository.get();
        String msg = buildMessage(order, settings);
        String sender = settings.getSenderNumber();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        boolean ordererValid = order.isSendPhotoToOrderer() && isValidPhone(order.getOrdererPhone());
        boolean recipientValid = order.isSendPhotoToRecipient() && isValidPhone(order.getRecipientPhone());
        boolean samePhone = ordererValid && recipientValid
                && order.getOrdererPhone().equals(order.getRecipientPhone());

        for (String imageUrl : urls) {
            if (ordererValid) {
                sendAndRecord(orderId, order.getOrdererPhone(), msg, imageUrl, sender, now);
            }
            if (recipientValid && !samePhone) {
                sendAndRecord(orderId, order.getRecipientPhone(), msg, imageUrl, sender, now);
            }
        }
    }

    @Override
    @Transactional
    public void resend(Long orderId) {
        List<SmsMessage> failed = smsMessageRepository.findAllByOrderId(orderId).stream()
                .filter(m -> m.getStatus() == SmsStatus.FAIL)
                .toList();
        if (failed.isEmpty()) return;

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        SmsSettings settings = smsSettingsRepository.get();
        String msg = buildMessage(order, settings);
        String sender = settings.getSenderNumber();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        for (SmsMessage m : failed) {
            sendAndRecord(orderId, m.getReceiver(), msg, m.getImageUrl(), sender, now);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmsMessageResult> getMessages() {
        return smsMessageRepository.findAll().stream().map(this::toResult).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmsMessageResult> getMessagesByOrder(Long orderId) {
        return smsMessageRepository.findAllByOrderId(orderId).stream().map(this::toResult).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<SmsMessageResult> getMessagesPaged(Pageable pageable) {
        return PageResult.of(smsMessageRepository.findAll(pageable).map(this::toResult));
    }

    @Override
    @Transactional
    public void notify(Long orderId, String vendorPhone) {
        if (vendorPhone == null || vendorPhone.isBlank()) return;
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

            SmsSettings settings = smsSettingsRepository.get();
            String sender = settings.getSenderNumber();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String msg = buildNotificationMessage(order);

            SmsMessage smsMessage = SmsMessage.createNotification(orderId, vendorPhone, msg);
            try {
                SmsResult result = smsClient.send(vendorPhone, msg, null, sender);
                if (result.success()) {
                    smsMessage.markSuccess(result.msgId(), now);
                } else {
                    smsMessage.markFail(result.failReason(), now);
                }
            } catch (Exception e) {
                smsMessage.markFail(e.getMessage(), now);
            }
            smsMessageRepository.save(smsMessage);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(SmsService.class)
                    .error("[발주 알림 SMS 실패] orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }

    private String buildNotificationMessage(Order order) {
        String deliveryInfo = order.getDeliveryDate() != null ? order.getDeliveryDate() : "-";
        if (order.getDeliveryTimeCustom() != null && !order.getDeliveryTimeCustom().isBlank()) {
            deliveryInfo += " " + order.getDeliveryTimeCustom();
        } else if (order.getDeliveryTimeHour() != null && order.getDeliveryTimeMinute() != null) {
            deliveryInfo += " " + order.getDeliveryTimeHour() + ":" + order.getDeliveryTimeMinute();
        }
        String address = order.getAddress() != null ? order.getAddress() : "-";
        if (order.getAddressDetail() != null && !order.getAddressDetail().isBlank()) {
            address += " " + order.getAddressDetail();
        }
        String link = (portalBaseUrl != null && !portalBaseUrl.isBlank())
                ? portalBaseUrl + "/vendor/orders/" + order.getId()
                : "/vendor/orders/" + order.getId();

        return "[온우림] 새 발주가 접수되었습니다.\n\n"
                + "주문번호: " + order.getOrderNumber() + "\n"
                + "주문자: " + order.getOrdererName() + " / " + order.getOrdererPhone() + "\n"
                + "배송지: " + address + "\n"
                + "배송요구일: " + deliveryInfo + "\n"
                + "상품명: " + order.getProductName() + " / " + order.getQuantity() + "개\n"
                + "발주금액: " + String.format("%,d", order.getAmount()) + "원\n\n"
                + "수주 확인 및 승인/거절:\n"
                + link;
    }

    private void sendAndRecord(Long orderId, String receiver, String msg, String imageUrl, String sender, String sentAt) {
        SmsMessage smsMessage = SmsMessage.create(orderId, receiver, msg, imageUrl);
        try {
            SmsResult result = smsClient.send(receiver, msg, imageUrl, sender);
            if (result.success()) {
                smsMessage.markSuccess(result.msgId(), sentAt);
            } else {
                smsMessage.markFail(result.failReason(), sentAt);
            }
        } catch (Exception e) {
            smsMessage.markFail(e.getMessage(), sentAt);
        }
        smsMessageRepository.save(smsMessage);
    }

    private String buildMessage(Order order, SmsSettings settings) {
        String template = settings.resolveTemplate(order.getEventType());
        String placeName = order.getPlaceName() != null ? order.getPlaceName() : order.getAddress();
        return template
                .replace("{업체명}", settings.getSenderName() != null ? settings.getSenderName() : "")
                .replace("{주문자명}", order.getOrdererName() != null ? order.getOrdererName() : "")
                .replace("{장소}", placeName != null ? placeName : "")
                .replace("{주문번호}", order.getId() != null ? String.valueOf(order.getId()) : "")
                .replace("{사진링크}", "");
    }

    private boolean isValidPhone(String phone) {
        return phone != null && !phone.isBlank();
    }

    private SmsMessageResult toResult(SmsMessage m) {
        return new SmsMessageResult(
                m.getId(), m.getOrderId(), m.getReceiver(),
                m.getMessage(), m.getStatus().name(), m.getFailReason(), m.getAligoMsgId(), m.getSentAt(), m.getImageUrl(),
                m.getType() != null ? m.getType().name() : "DELIVERY"
        );
    }
}
