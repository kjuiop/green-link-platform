package com.greenlink.core.application.company.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.OrderingFeeResult;
import com.greenlink.core.application.company.usecase.ManageOrderingFeeUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.model.OrderingFeeDiscountType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.company.CompanyErrorCode;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderingFeeService implements ManageOrderingFeeUseCase {

    private final CompanyRepository companyRepository;

    public OrderingFeeService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public PageResult<OrderingFeeResult> getOrderingFees(String keyword, Pageable pageable) {
        return PageResult.of(companyRepository.searchOrderingCompanies(keyword, pageable).map(OrderingFeeResult::from));
    }

    @Override
    @Transactional
    public OrderingFeeResult updateOrderingFee(Long companyId, OrderingFeeDiscountType discountType, long discountValue) {
        Company company = companyRepository.findById(companyId)
                .filter(c -> c.getType() == CompanyType.ORDERING || c.getType() == CompanyType.BOTH)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.ORDERING_COMPANY_NOT_FOUND));
        company.changeOrderingFeeDiscount(discountType, discountValue);
        return OrderingFeeResult.from(companyRepository.save(company));
    }
}
