package com.greenlink.core.domain.settlement.port;

import java.util.List;

public interface SettlementQueryPort {

    List<CompanySettlementRaw> sumByVendorCompany(String fromDate, String toDate);
    List<CompanySettlementRaw> sumByOrderingCompany(String fromDate, String toDate);
    List<OrderSettlementRaw> findDeliveredOrdersByVendorCompany(Long vendorCompanyId, String fromDate, String toDate);
    List<OrderSettlementRaw> findDeliveredOrdersByOrderingCompany(Long orderingCompanyId, String fromDate, String toDate);

    record CompanySettlementRaw(Long companyId, long count, long orderAmountSum, long vendorAmountSum) {}

    record OrderSettlementRaw(
            Long orderId,
            String orderNumber,
            Long counterpartCompanyId,
            String productName,
            String placeName,
            String ordererName,
            String completedAt,
            long orderAmount,
            Long vendorAmount,
            Long extraCost
    ) {}
}
