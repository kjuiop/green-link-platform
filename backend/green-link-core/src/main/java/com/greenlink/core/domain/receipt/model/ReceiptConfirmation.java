package com.greenlink.core.domain.receipt.model;

import java.time.LocalDateTime;
import java.util.List;

public class ReceiptConfirmation {

    private Long id;
    private Long orderId;
    private String receiverName;
    private String receiverRelation;
    private boolean delivered;
    private String memo;
    private LocalDateTime confirmedAt;
    private List<String> photoUrls;

    private ReceiptConfirmation() {}

    public static ReceiptConfirmation create(Long orderId, String receiverName, String receiverRelation,
                                              boolean delivered, String memo, LocalDateTime confirmedAt,
                                              List<String> photoUrls) {
        ReceiptConfirmation rc = new ReceiptConfirmation();
        rc.orderId = orderId;
        rc.receiverName = receiverName;
        rc.receiverRelation = receiverRelation;
        rc.delivered = delivered;
        rc.memo = memo;
        rc.confirmedAt = confirmedAt;
        rc.photoUrls = photoUrls;
        return rc;
    }

    public static ReceiptConfirmation of(Long id, Long orderId, String receiverName, String receiverRelation,
                                          boolean delivered, String memo, LocalDateTime confirmedAt,
                                          List<String> photoUrls) {
        ReceiptConfirmation rc = create(orderId, receiverName, receiverRelation, delivered, memo, confirmedAt, photoUrls);
        rc.id = id;
        return rc;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverRelation() { return receiverRelation; }
    public boolean isDelivered() { return delivered; }
    public String getMemo() { return memo; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public List<String> getPhotoUrls() { return photoUrls; }
}
