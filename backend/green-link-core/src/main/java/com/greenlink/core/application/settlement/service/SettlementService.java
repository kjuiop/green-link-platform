package com.greenlink.core.application.settlement.service;

import com.greenlink.core.application.settlement.dto.AdminSettlementQuery;
import com.greenlink.core.application.settlement.dto.SettlementOrderResult;
import com.greenlink.core.application.settlement.dto.SettlementSummaryResult;
import com.greenlink.core.application.settlement.usecase.*;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.settlement.model.Settlement;
import com.greenlink.core.domain.settlement.model.SettlementType;
import com.greenlink.core.domain.settlement.port.SettlementQueryPort;
import com.greenlink.core.domain.settlement.port.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SettlementService implements
        GetAdminSettlementsUseCase,
        GetAdminSettlementDetailUseCase,
        CompleteSettlementUseCase,
        CancelSettlementUseCase,
        GetMySettlementUseCase {

    private final SettlementRepository settlementRepository;
    private final SettlementQueryPort settlementQueryPort;
    private final CompanyRepository companyRepository;

    public SettlementService(SettlementRepository settlementRepository,
                              SettlementQueryPort settlementQueryPort,
                              CompanyRepository companyRepository) {
        this.settlementRepository = settlementRepository;
        this.settlementQueryPort = settlementQueryPort;
        this.companyRepository = companyRepository;
    }

    @Override
    public List<SettlementSummaryResult> getAdminSettlements(AdminSettlementQuery query) {
        List<SettlementQueryPort.CompanySettlementRaw> raws = query.type() == SettlementType.VENDOR
                ? settlementQueryPort.sumByVendorCompany(query.fromDate(), query.toDate())
                : settlementQueryPort.sumByOrderingCompany(query.fromDate(), query.toDate());

        List<Long> companyIds = raws.stream().map(SettlementQueryPort.CompanySettlementRaw::companyId).toList();
        Map<Long, String> nameMap = companyRepository.findAllByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Company::getCompanyName));

        List<Long> settledIds = settlementRepository
                .findAllByCompanyIdsAndTypeAndPeriod(companyIds, query.type(), query.fromDate(), query.toDate())
                .stream().map(Settlement::getCompanyId).toList();
        Map<Long, String> completedAtMap = settlementRepository
                .findAllByCompanyIdsAndTypeAndPeriod(companyIds, query.type(), query.fromDate(), query.toDate())
                .stream().collect(Collectors.toMap(Settlement::getCompanyId, Settlement::getCompletedAt, (a, b) -> a));

        return raws.stream()
                .map(r -> new SettlementSummaryResult(
                        r.companyId(),
                        nameMap.getOrDefault(r.companyId(), ""),
                        (int) r.count(),
                        r.orderAmountSum(),
                        r.vendorAmountSum(),
                        settledIds.contains(r.companyId()),
                        completedAtMap.get(r.companyId())
                ))
                .toList();
    }

    @Override
    public List<SettlementOrderResult> getAdminSettlementDetail(Long companyId, AdminSettlementQuery query) {
        List<SettlementQueryPort.OrderSettlementRaw> raws = query.type() == SettlementType.VENDOR
                ? settlementQueryPort.findDeliveredOrdersByVendorCompany(companyId, query.fromDate(), query.toDate())
                : settlementQueryPort.findDeliveredOrdersByOrderingCompany(companyId, query.fromDate(), query.toDate());

        List<Long> counterpartIds = raws.stream()
                .map(SettlementQueryPort.OrderSettlementRaw::counterpartCompanyId)
                .filter(id -> id != null)
                .distinct().toList();
        Map<Long, String> nameMap = companyRepository.findAllByIdIn(counterpartIds).stream()
                .collect(Collectors.toMap(Company::getId, Company::getCompanyName));

        return raws.stream()
                .map(r -> new SettlementOrderResult(
                        r.orderId(),
                        r.orderNumber(),
                        r.counterpartCompanyId() != null ? nameMap.getOrDefault(r.counterpartCompanyId(), "") : "",
                        r.productName(),
                        r.placeName(),
                        r.ordererName(),
                        r.completedAt(),
                        r.orderAmount(),
                        r.vendorAmount()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void completeSettlement(Long companyId, SettlementType type, String fromDate, String toDate, Long adminId) {
        boolean exists = settlementRepository
                .findByCompanyIdAndTypeAndPeriod(companyId, type, fromDate, toDate)
                .isPresent();
        if (!exists) {
            String now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            settlementRepository.save(Settlement.complete(companyId, type, fromDate, toDate, now, adminId));
        }
    }

    @Override
    @Transactional
    public void cancelSettlement(Long companyId, SettlementType type, String fromDate, String toDate) {
        settlementRepository.deleteByCompanyIdAndTypeAndPeriod(companyId, type, fromDate, toDate);
    }

    @Override
    public List<SettlementOrderResult> getMySettlement(Long companyId, SettlementType type, String fromDate, String toDate) {
        List<SettlementQueryPort.OrderSettlementRaw> raws = type == SettlementType.VENDOR
                ? settlementQueryPort.findDeliveredOrdersByVendorCompany(companyId, fromDate, toDate)
                : settlementQueryPort.findDeliveredOrdersByOrderingCompany(companyId, fromDate, toDate);

        List<Long> counterpartIds = raws.stream()
                .map(SettlementQueryPort.OrderSettlementRaw::counterpartCompanyId)
                .filter(id -> id != null)
                .distinct().toList();
        Map<Long, String> nameMap = companyRepository.findAllByIdIn(counterpartIds).stream()
                .collect(Collectors.toMap(Company::getId, Company::getCompanyName));

        return raws.stream()
                .map(r -> new SettlementOrderResult(
                        r.orderId(),
                        r.orderNumber(),
                        r.counterpartCompanyId() != null ? nameMap.getOrDefault(r.counterpartCompanyId(), "") : "",
                        r.productName(),
                        r.placeName(),
                        r.ordererName(),
                        r.completedAt(),
                        r.orderAmount(),
                        r.vendorAmount()
                ))
                .toList();
    }
}
