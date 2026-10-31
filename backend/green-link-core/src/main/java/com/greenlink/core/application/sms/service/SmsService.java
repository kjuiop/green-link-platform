package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.sms.dto.SmsMessageResult;
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
        String sender = settings.getSenderNumber();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String[] schedule = resolveSchedule(order);
        String rdate = schedule[0];
        String rtime = schedule[1];

        boolean ordererValid = order.isSendPhotoToOrderer() && isValidPhone(order.getOrdererPhone());
        boolean recipientValid = order.isSendPhotoToRecipient() && isValidPhone(order.getRecipientPhone());
        boolean samePhone = ordererValid && recipientValid
                && order.getOrdererPhone().equals(order.getRecipientPhone());

        for (String imageUrl : urls) {
            if (ordererValid) {
                sendAndRecord(orderId, order.getOrdererPhone(), msg, imageUrl, sender, now, rdate, rtime);
            }
            if (recipientValid && !samePhone) {
                sendAndRecord(orderId, order.getRecipientPhone(), msg, imageUrl, sender, now, rdate, rtime);
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
            sendAndRecord(orderId, m.getReceiver(), msg, m.getImageUrl(), sender, now, null, null);
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
            String sender = settings.getSenderNumber();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String msg = buildNotificationMessage(order, vendorName, orderingCompanyName);

            SmsMessage smsMessage = SmsMessage.createNotification(orderId, vendorPhone, msg);
            try {
                SmsResult result = smsClient.send(vendorPhone, msg, null, sender, null, null, title);
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

        return "주문번호: " + order.getOrderNumber() + "\n"
                + orderingLine
                + vendorLine
                + "주문자: " + order.getOrdererName() + " / " + order.getOrdererPhone() + "\n"
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

    private void sendAndRecord(Long orderId, String receiver, String msg, String imageUrl, String sender, String sentAt, String rdate, String rtime) {
        SmsMessage smsMessage = SmsMessage.create(orderId, receiver, msg, imageUrl);
        try {
            SmsResult result = smsClient.send(receiver, msg, imageUrl, sender, rdate, rtime, null);
            boolean reserved = rdate != null && !rdate.isBlank();
            if (reserved && result.rejectedByProvider()) {
                // 알리고 예약 최소 간격 등으로 예약이 거절되면 즉시발송으로 폴백
                org.slf4j.LoggerFactory.getLogger(SmsService.class)
                        .warn("알리고 예약발송 거절 → 즉시발송 폴백 orderId={} rdate={} rtime={} reason={}", orderId, rdate, rtime, result.failReason());
                result = smsClient.send(receiver, msg, imageUrl, sender, null, null, null);
            }
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
