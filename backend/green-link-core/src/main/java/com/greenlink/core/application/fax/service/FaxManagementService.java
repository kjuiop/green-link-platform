package com.greenlink.core.application.fax.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.fax.dto.FaxMessageResult;
import com.greenlink.core.application.fax.dto.FaxVendorResult;
import com.greenlink.core.application.fax.usecase.ManageFaxUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.fax.model.FaxStatus;
import com.greenlink.core.domain.fax.port.FaxMessageRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.fax.FaxErrorCode;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FaxManagementService implements ManageFaxUseCase {

    private final CompanyRepository companyRepository;
    private final FaxMessageRepository faxMessageRepository;
    private final OrderRepository orderRepository;

    public FaxManagementService(CompanyRepository companyRepository,
                                FaxMessageRepository faxMessageRepository,
                                OrderRepository orderRepository) {
        this.companyRepository = companyRepository;
        this.faxMessageRepository = faxMessageRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public PageResult<FaxVendorResult> getVendors(String keyword, Pageable pageable) {
        return PageResult.of(companyRepository.searchVendorCompanies(keyword, pageable).map(FaxVendorResult::from));
    }

    @Override
    @Transactional
    public FaxVendorResult updateVendor(Long companyId, boolean faxEnabled, String faxNumber) {
        Company company = companyRepository.findById(companyId)
                .filter(c -> c.getType() == CompanyType.VENDOR || c.getType() == CompanyType.BOTH)
                .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
        try {
            company.changeFaxSetting(faxEnabled, faxNumber);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(FaxErrorCode.FAX_NUMBER_REQUIRED);
        }
        return FaxVendorResult.from(companyRepository.save(company));
    }

    @Override
    public PageResult<FaxMessageResult> getMessages(String status, String keyword, Pageable pageable) {
        FaxStatus faxStatus = parseStatus(status);
        var page = faxMessageRepository.search(faxStatus, keyword, pageable);
        List<Long> orderIds = page.getContent().stream().map(m -> m.getOrderId()).distinct().toList();
        Map<Long, Order> orders = orderRepository.findAllByIds(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity(), (a, b) -> a));
        List<Long> companyIds = new java.util.ArrayList<>(new HashSet<>(
                page.getContent().stream().map(m -> m.getVendorCompanyId()).toList()));
        Map<Long, String> companyNames = companyRepository.findAllByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Company::getCompanyName, (a, b) -> a));
        return PageResult.of(page.map(m -> FaxMessageResult.of(m,
                orders.containsKey(m.getOrderId()) ? orders.get(m.getOrderId()).getOrderNumber() : null,
                companyNames.get(m.getVendorCompanyId()))));
    }

    private FaxStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return FaxStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
