package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.sms.dto.SmsMessageResult;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import com.greenlink.core.domain.sms.model.SmsResult;
import com.greenlink.core.application.sms.usecase.GetSmsMessagesUseCase;
import com.greenlink.core.application.sms.usecase.ResendMmsUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryMmsUseCase;
import com.greenlink.core.application.sms.usecase.SendOrderNotificationUseCase;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsMessageType;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsClientFactory;
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
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@Service
public class SmsService implements SendDeliveryMmsUseCase, ResendMmsUseCase, GetSmsMessagesUseCase, SendOrderNotificationUseCase {

    private static final String NOTIFICATION_TITLE = "[온우림] 새 발주가 접수되었습니다.";

    private final OrderRepository orderRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsClientFactory smsClientFactory;
    private final SmsSettingsRepository smsSettingsRepository;
    private final CompanyIntegrationRepository companyIntegrationRepository;

    @Value("${app.portal-base-url:}")
    private String portalBaseUrl;

    public SmsService(OrderRepository orderRepository,
                      SmsMessageRepository smsMessageRepository,
                      SmsClientFactory smsClientFactory,
                      SmsSettingsRepository smsSettingsRepository,
                      CompanyIntegrationRepository companyIntegrationRepository) {
        this.orderRepository = orderRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.smsClientFactory = smsClientFactory;
        this.smsSettingsRepository = smsSettingsRepository;
        this.companyIntegrationRepository = companyIntegrationRepository;
    }

    @Override
    @Transactional
    public void send(Long orderId, List<String> photoUrls) {
        boolean alreadySent = smsMessageRepository.findAllByOrderId(orderId).stream()
                .anyMatch(m -> m.getType() == SmsMessageType.DELIVERY && m.getStatus() == SmsStatus.SUCCESS);
        if (alreadySent) {
            return;
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        List<String> urls = (photoUrls != null && !photoUrls.isEmpty())
                ? photoUrls
                : Collections.singletonList(null);

        SmsSettings settings = smsSettingsRepository.get();
        String msg = buildMessage(order, settings);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String[] schedule = resolveSchedule(order);
        String rdate = schedule[0];
        String rtime = schedule[1];

        CompanyConfig companyConfig = resolveCompanyConfig(order.getOrderingCompanyId(), settings);

        boolean ordererValid = order.isSendPhotoToOrderer() && isValidPhone(order.getOrdererPhone());
        boolean recipientValid = order.isSendPhotoToRecipient() && isValidPhone(order.getRecipientPhone());
        boolean samePhone = ordererValid && recipientValid
                && order.getOrdererPhone().equals(order.getRecipientPhone());

        for (String imageUrl : urls) {
            if (ordererValid) {
                sendAndRecord(orderId, order.getOrdererPhone(), msg, imageUrl, companyConfig, now, rdate, rtime);
            }
            if (recipientValid && !samePhone) {
                sendAndRecord(orderId, order.getRecipientPhone(), msg, imageUrl, companyConfig, now, rdate, rtime);
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
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        CompanyConfig companyConfig = resolveCompanyConfig(order.getOrderingCompanyId(), settings);

        for (SmsMessage m : failed) {
            sendAndRecord(orderId, m.getReceiver(), msg, m.getImageUrl(), companyConfig, now, null, null);
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
    public void notify(Long orderId, String vendorPhone, String vendorName, String orderingCompanyName, String notificationTitle) {
        if (vendorPhone == null || vendorPhone.isBlank()) return;
        String title = (notificationTitle != null && !notificationTitle.isBlank()) ? notificationTitle : NOTIFICATION_TITLE;
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

            SmsSettings settings = smsSettingsRepository.get();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String msg = buildNotificationMessage(order, vendorName, orderingCompanyName);

            CompanyConfig companyConfig = new CompanyConfig(settings.getProvider(), settings.getApiKey(), settings.getSenderNumber());
            SmsClient client = smsClientFactory.getByProvider(companyConfig.provider);

            SmsMessage smsMessage = SmsMessage.createNotification(orderId, vendorPhone, msg);
            try {
                SmsResult result = dispatchSend(client, companyConfig, vendorPhone, msg, null, null, null, title);
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

    private CompanyConfig resolveCompanyConfig(Long companyId, SmsSettings fallback) {
        if (companyId != null) {
            return companyIntegrationRepository.findByCompanyId(companyId)
                    .map(ci -> new CompanyConfig(ci.getProvider(), ci.getApiKey(),
                            ci.getSenderNumber() != null ? ci.getSenderNumber() : fallback.getSenderNumber()))
                    .orElseGet(() -> new CompanyConfig(fallback.getProvider(), fallback.getApiKey(), fallback.getSenderNumber()));
        }
        return new CompanyConfig(fallback.getProvider(), fallback.getApiKey(), fallback.getSenderNumber());
    }

    private SmsResult dispatchSend(SmsClient client, CompanyConfig config,
                                   String receiver, String msg, String imageUrl,
                                   String rdate, String rtime, String title) {
        if ("MESSAGEME".equalsIgnoreCase(config.provider)) {
            String sendReserve = (rdate != null && !rdate.isBlank()) ? "1" : "0";
            String sendReserveDate = formatReserveDate(rdate, rtime);
            return client.sendWithCredentials(config.apiKey, config.senderNumber,
                    receiver, msg, imageUrl, sendReserve, sendReserveDate);
        }
        return client.send(receiver, msg, imageUrl, config.senderNumber, rdate, rtime, title);
    }

    private String formatReserveDate(String rdate, String rtime) {
        if (rdate == null || rdate.length() < 8) return null;
        String year  = rdate.substring(0, 4);
        String month = rdate.substring(4, 6);
        String day   = rdate.substring(6, 8);
        String hour  = (rtime != null && rtime.length() >= 2) ? rtime.substring(0, 2) : "00";
        String min   = (rtime != null && rtime.length() >= 4) ? rtime.substring(2, 4) : "00";
        return year + "|" + month + "|" + day + "|" + hour + "|" + min + "|00";
    }

    private void sendAndRecord(Long orderId, String receiver, String msg, String imageUrl,
                               CompanyConfig companyConfig, String sentAt, String rdate, String rtime) {
        SmsClient client = smsClientFactory.getByProvider(companyConfig.provider);
        SmsMessage smsMessage = SmsMessage.create(orderId, receiver, msg, imageUrl);
        try {
            SmsResult result = dispatchSend(client, companyConfig, receiver, msg, imageUrl, rdate, rtime, null);
            String fallbackReason = null;
            boolean reserved = rdate != null && !rdate.isBlank();
            if (reserved && result.rejectedByProvider() && "ALIGO".equalsIgnoreCase(companyConfig.provider)) {
                org.slf4j.LoggerFactory.getLogger(SmsService.class)
                        .warn("알리고 예약발송 거절 → 즉시발송 폴백 orderId={} rdate={} rtime={} reason={}", orderId, rdate, rtime, result.failReason());
                fallbackReason = result.failReason();
                result = client.send(receiver, msg, imageUrl, companyConfig.senderNumber, null, null, null);
            }
            if (result.success() && fallbackReason != null) {
                smsMessage.markSuccessWithFallback(result.msgId(), sentAt, fallbackReason);
            } else if (result.success()) {
                smsMessage.markSuccess(result.msgId(), sentAt);
            } else {
                smsMessage.markFail(result.failReason(), sentAt);
            }
        } catch (Exception e) {
            smsMessage.markFail(e.getMessage(), sentAt);
        }
        smsMessageRepository.save(smsMessage);
    }

    private record CompanyConfig(String provider, String apiKey, String senderNumber) {}

    private String buildNotificationMessage(Order order, String vendorName, String orderingCompanyName) {
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

        String orderingLine = (orderingCompanyName != null && !orderingCompanyName.isBlank())
                ? "발주사: " + orderingCompanyName + "\n" : "";
        String vendorLine = (vendorName != null && !vendorName.isBlank())
                ? "수주업체: " + vendorName + "\n" : "";

        String recipientName = order.getRecipientName() != null && !order.getRecipientName().isBlank()
                ? order.getRecipientName() : order.getOrdererName();
        String recipientPhoneLine = (order.getRecipientPhone() != null && !order.getRecipientPhone().isBlank())
                ? "받는 이 연락처: " + order.getRecipientPhone() + "\n" : "";

        return "주문번호: " + order.getOrderNumber() + "\n"
                + orderingLine
                + vendorLine
                + "받는 이: " + recipientName + "\n"
                + recipientPhoneLine
                + "배송지: " + address + "\n"
                + "배송요구일: " + deliveryInfo + "\n"
                + "상품명: " + order.getProductName() + " / " + order.getQuantity() + "개\n"
                + "발주금액: " + String.format("%,d", order.getAmount()) + "원\n\n"
                + "수주 확인 및 승인/거절:\n"
                + link;
    }

    private String[] resolveSchedule(Order order) {
        if (!"SCHEDULED".equalsIgnoreCase(order.getMessageSchedule())) {
            return new String[]{null, null};
        }
        String deliveryDate = order.getDeliveryDate();
        String hour = order.getScheduledMessageHour();
        String minute = order.getScheduledMessageMinute();
        if (deliveryDate == null || hour == null || minute == null) {
            return new String[]{null, null};
        }
        try {
            String dateOnly = deliveryDate.length() >= 10 ? deliveryDate.substring(0, 10) : deliveryDate;
            String rdate = dateOnly.replace("-", "");
            String rtime = String.format("%02d%02d", Integer.parseInt(hour), Integer.parseInt(minute));
            LocalDateTime scheduledAt = LocalDateTime.parse(dateOnly + "T" + String.format("%02d:%02d:00", Integer.parseInt(hour), Integer.parseInt(minute)));
            if (scheduledAt.isBefore(LocalDateTime.now())) {
                return new String[]{null, null};
            }
            return new String[]{rdate, rtime};
        } catch (DateTimeParseException | NumberFormatException e) {
            return new String[]{null, null};
        }
    }

    private boolean isValidPhone(String phone) {
        return phone != null && !phone.isBlank();
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

    private SmsMessageResult toResult(SmsMessage m) {
        return new SmsMessageResult(
                m.getId(), m.getOrderId(), m.getReceiver(),
                m.getMessage(), m.getStatus().name(), m.getFailReason(), m.getAligoMsgId(), m.getSentAt(), m.getImageUrl(),
                m.getType() != null ? m.getType().name() : "DELIVERY"
        );
    }
}
