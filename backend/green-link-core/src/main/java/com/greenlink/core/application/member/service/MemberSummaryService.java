package com.greenlink.core.application.member.service;

import com.greenlink.core.application.member.dto.CompanyAccountResult;
import com.greenlink.core.application.member.dto.MemberSummaryResult;
import com.greenlink.core.application.member.usecase.GetMemberSummaryUseCase;
import com.greenlink.core.application.member.usecase.GetMyAccountUseCase;
import com.greenlink.core.application.member.usecase.UpdateBusinessStatusUseCase;
import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.member.MemberErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
public class MemberSummaryService implements GetMemberSummaryUseCase, GetMyAccountUseCase, UpdateBusinessStatusUseCase {

    private final CompanyRepository companyRepository;
    private final OrderRepository orderRepository;

    public MemberSummaryService(CompanyRepository companyRepository, OrderRepository orderRepository) {
        this.companyRepository = companyRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public MemberSummaryResult getMemberSummary(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.COMPANY_NOT_FOUND));

        YearMonth thisMonth = YearMonth.now(ZoneId.of("Asia/Seoul"));
        String dateFrom = thisMonth.atDay(1).toString();
        String dateTo = thisMonth.atEndOfMonth().toString();

        CompanyType type = company.getType();

        long orderingAmount = 0L;
        if (type == CompanyType.ORDERING || type == CompanyType.BOTH) {
            OrderSearchCondition orderingCondition = new OrderSearchCondition(
                    null, null, null, null, null,
                    dateFrom, dateTo,
                    companyId,
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null
            );
            orderingAmount = orderRepository.sumAmount(orderingCondition);
        }

        long vendorAmount = 0L;
        if (type == CompanyType.VENDOR || type == CompanyType.BOTH) {
            OrderSearchCondition vendorCondition = new OrderSearchCondition(
                    null, null, null, null, null,
                    dateFrom, dateTo,
                    null,
                    null, null, null, null, null, null, null, null, null, null,
                    companyId,
                    null, null
            );
            vendorAmount = orderRepository.sumAmount(vendorCondition);
        }

        return new MemberSummaryResult(type.name(), orderingAmount, vendorAmount, null);
    }

    @Override
    public CompanyAccountResult getMyAccount(Long companyId) {
        return companyRepository.findById(companyId)
                .map(c -> new CompanyAccountResult(c.getBankName(), c.getAccountNumber(), c.getAccountHolder()))
                .orElseThrow(() -> new BusinessException(MemberErrorCode.COMPANY_NOT_FOUND));
    }

    @Override
    @Transactional
    public void updateBusinessStatus(Long companyId, String status) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.COMPANY_NOT_FOUND));
        company.updateBusinessStatus(BusinessStatus.valueOf(status.toUpperCase()));
        companyRepository.save(company);
    }
}
