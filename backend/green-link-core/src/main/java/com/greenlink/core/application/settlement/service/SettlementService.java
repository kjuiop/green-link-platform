package com.greenlink.core.application.settlement.service;

import com.greenlink.core.application.settlement.dto.AdminSettlementQuery;
import com.greenlink.core.application.settlement.dto.MySettlementResult;
import com.greenlink.core.application.settlement.dto.SettlementOrderResult;
import com.greenlink.core.application.settlement.dto.SettlementSummaryResult;
import com.greenlink.core.application.settlement.usecase.*;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
        if (query.type() == SettlementType.BOTH) {
            return getAdminSettlementsBoth(query);
        }

        List<SettlementQueryPort.CompanySettlementRaw> raws;

        if (query.type() == SettlementType.ALL) {
            List<SettlementQueryPort.CompanySettlementRaw> vendorRaws =
                    settlementQueryPort.sumByVendorCompany(query.fromDate(), query.toDate());
            List<SettlementQueryPort.CompanySettlementRaw> orderingRaws =
                    settlementQueryPort.sumByOrderingCompany(query.fromDate(), query.toDate());
            Map<Long, SettlementQueryPort.CompanySettlementRaw> merged = new LinkedHashMap<>();
            for (var r : vendorRaws) merged.put(r.companyId(), r);
            for (var r : orderingRaws) {
                merged.merge(r.companyId(), r, (a, b) -> new SettlementQueryPort.CompanySettlementRaw(
                        a.companyId(),
                        a.count() + b.count(),
                        a.orderAmountSum() + b.orderAmountSum(),
                        a.vendorAmountSum() + b.vendorAmountSum()
                ));
            }
            raws = new ArrayList<>(merged.values());
        } else {
            raws = query.type() == SettlementType.VENDOR
                    ? settlementQueryPort.sumByVendorCompany(query.fromDate(), query.toDate())
                    : settlementQueryPort.sumByOrderingCompany(query.fromDate(), query.toDate());
        }

        List<Long> companyIds = raws.stream().map(SettlementQueryPort.CompanySettlementRaw::companyId).toList();
        Map<Long, Company> companyMap = companyRepository.findAllByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Function.identity()));

        Set<Long> settledIds;
        Map<Long, String> completedAtMap;
        if (query.type() == SettlementType.ALL) {
            settledIds = Set.of();
            completedAtMap = Map.of();
        } else {
            List<Settlement> settled = settlementRepository
                    .findAllByCompanyIdsAndTypeAndPeriod(companyIds, query.type(), query.fromDate(), query.toDate());
            settledIds = settled.stream().map(Settlement::getCompanyId).collect(Collectors.toSet());
            completedAtMap = settled.stream()
                    .collect(Collectors.toMap(Settlement::getCompanyId, Settlement::getCompletedAt, (a, b) -> a));
        }

        final Set<Long> finalSettledIds = settledIds;
        final Map<Long, String> finalCompletedAtMap = completedAtMap;

        return raws.stream()
                .map(r -> {
                    Company company = companyMap.get(r.companyId());
                    return new SettlementSummaryResult(
                            r.companyId(),
                            company != null ? company.getCompanyName() : "",
                            company != null ? company.getType().name() : "",
                            (int) r.count(),
                            r.orderAmountSum(),
                            r.vendorAmountSum(),
                            finalSettledIds.contains(r.companyId()),
                            finalCompletedAtMap.get(r.companyId())
                    );
                })
                .toList();
    }

    private List<SettlementSummaryResult> getAdminSettlementsBoth(AdminSettlementQuery query) {
        List<Long> bothIds = companyRepository.findAllByType(CompanyType.BOTH)
                .stream().map(Company::getId).toList();
        if (bothIds.isEmpty()) return List.of();

        Set<Long> bothIdSet = new java.util.HashSet<>(bothIds);

        Map<Long, SettlementQueryPort.CompanySettlementRaw> vendorMap =
                settlementQueryPort.sumByVendorCompany(query.fromDate(), query.toDate())
                        .stream().filter(r -> bothIdSet.contains(r.companyId()))
                        .collect(Collectors.toMap(SettlementQueryPort.CompanySettlementRaw::companyId, Function.identity()));

        Map<Long, SettlementQueryPort.CompanySettlementRaw> orderingMap =
                settlementQueryPort.sumByOrderingCompany(query.fromDate(), query.toDate())
                        .stream().filter(r -> bothIdSet.contains(r.companyId()))
                        .collect(Collectors.toMap(SettlementQueryPort.CompanySettlementRaw::companyId, Function.identity()));

        Set<Long> allIds = new LinkedHashSet<>(vendorMap.keySet());
        allIds.addAll(orderingMap.keySet());
        if (allIds.isEmpty()) return List.of();

        Map<Long, Company> companyMap = companyRepository.findAllByIdIn(new ArrayList<>(allIds)).stream()
                .collect(Collectors.toMap(Company::getId, Function.identity()));

        SettlementQueryPort.CompanySettlementRaw zero = new SettlementQueryPort.CompanySettlementRaw(0L, 0, 0, 0);
        return allIds.stream().map(id -> {
            SettlementQueryPort.CompanySettlementRaw v = vendorMap.getOrDefault(id, zero);
            SettlementQueryPort.CompanySettlementRaw o = orderingMap.getOrDefault(id, zero);
            Company company = companyMap.get(id);
            return new SettlementSummaryResult(
                    id,
                    company != null ? company.getCompanyName() : "",
                    "BOTH",
                    (int)(v.count() + o.count()),
                    o.orderAmountSum(),   // 발주 지급액
                    v.vendorAmountSum(),  // 수주 수령액
                    false,
                    null
            );
        }).toList();
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
    public MySettlementResult getMySettlement(Long companyId, String fromDate, String toDate) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found: " + companyId));
        CompanyType type = company.getType();

        List<SettlementOrderResult> vendorOrders = type == CompanyType.VENDOR || type == CompanyType.BOTH
                ? toOrderResults(settlementQueryPort.findDeliveredOrdersByVendorCompany(companyId, fromDate, toDate))
                : List.of();

        List<SettlementOrderResult> orderingOrders = type == CompanyType.ORDERING || type == CompanyType.BOTH
                ? toOrderResults(settlementQueryPort.findDeliveredOrdersByOrderingCompany(companyId, fromDate, toDate))
                : List.of();

        return new MySettlementResult(type.name(), vendorOrders, orderingOrders);
    }

    private List<SettlementOrderResult> toOrderResults(List<SettlementQueryPort.OrderSettlementRaw> raws) {
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
