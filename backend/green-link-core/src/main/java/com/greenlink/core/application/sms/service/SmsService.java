package com.greenlink.core.application.sms.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.sms.dto.SmsMessageResult;
import com.greenlink.core.application.sms.dto.SmsResult;
import com.greenlink.core.application.sms.usecase.GetSmsMessagesUseCase;
import com.greenlink.core.application.sms.usecase.ResendMmsUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryMmsUseCase;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
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
public class SmsService implements SendDeliveryMmsUseCase, ResendMmsUseCase, GetSmsMessagesUseCase {

    private final OrderRepository orderRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsClient smsClient;

    public SmsService(OrderRepository orderRepository,
                      SmsMessageRepository smsMessageRepository,
                      SmsClient smsClient) {
        this.orderRepository = orderRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.smsClient = smsClient;
    }

    @Override
    @Transactional
    public void send(Long orderId, List<String> photoUrls) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // 사진 없으면 null 1건으로 처리 (사진 없는 SMS 1건 발송)
        List<String> urls = (photoUrls != null && !photoUrls.isEmpty())
                ? photoUrls
                : Collections.singletonList(null);

        String msg = buildMessage(order);
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        boolean ordererValid = order.isSendPhotoToOrderer() && isValidPhone(order.getOrdererPhone());
        boolean recipientValid = order.isSendPhotoToRecipient() && isValidPhone(order.getRecipientPhone());
        boolean samePhone = ordererValid && recipientValid
                && order.getOrdererPhone().equals(order.getRecipientPhone());

        for (String imageUrl : urls) {
            if (ordererValid) {
                sendAndRecord(orderId, order.getOrdererPhone(), msg, imageUrl, now);
            }
            if (recipientValid && !samePhone) {
                sendAndRecord(orderId, order.getRecipientPhone(), msg, imageUrl, now);
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
        String msg = buildMessage(order);
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        for (SmsMessage m : failed) {
            sendAndRecord(orderId, m.getReceiver(), msg, m.getImageUrl(), now);
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

    private void sendAndRecord(Long orderId, String receiver, String msg, String imageUrl, String sentAt) {
        SmsMessage smsMessage = SmsMessage.create(orderId, receiver, imageUrl);
        try {
            SmsResult result = smsClient.send(receiver, msg, imageUrl);
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

    private String buildMessage(Order order) {
        return String.format("[다운플라워] %s 배송이 완료되었습니다. 배송 사진을 확인해 주세요.", order.getOrdererName());
    }

    private boolean isValidPhone(String phone) {
        return phone != null && !phone.isBlank();
    }

    private SmsMessageResult toResult(SmsMessage m) {
        return new SmsMessageResult(
                m.getId(), m.getOrderId(), m.getReceiver(),
                m.getStatus().name(), m.getFailReason(), m.getAligoMsgId(), m.getSentAt(), m.getImageUrl()
        );
    }
}
