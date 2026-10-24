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
    private List<String> sitePhotoUrls;
    private List<String> storePhotoUrls;

    private ReceiptConfirmation() {}

    public static ReceiptConfirmation create(Long orderId, String receiverName, String receiverRelation,
                                              boolean delivered, String memo, LocalDateTime confirmedAt,
                                              List<String> sitePhotoUrls, List<String> storePhotoUrls) {
        ReceiptConfirmation rc = new ReceiptConfirmation();
        rc.orderId = orderId;
        rc.receiverName = receiverName;
        rc.receiverRelation = receiverRelation;
        rc.delivered = delivered;
        rc.memo = memo;
        rc.confirmedAt = confirmedAt;
        rc.sitePhotoUrls = sitePhotoUrls;
        rc.storePhotoUrls = storePhotoUrls;
        return rc;
    }

    public static ReceiptConfirmation of(Long id, Long orderId, String receiverName, String receiverRelation,
                                          boolean delivered, String memo, LocalDateTime confirmedAt,
                                          List<String> sitePhotoUrls, List<String> storePhotoUrls) {
        ReceiptConfirmation rc = create(orderId, receiverName, receiverRelation, delivered, memo, confirmedAt, sitePhotoUrls, storePhotoUrls);
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
    public List<String> getSitePhotoUrls() { return sitePhotoUrls; }
    public List<String> getStorePhotoUrls() { return storePhotoUrls; }
}
