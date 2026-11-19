package com.greenlink.core.application.company.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.VendorFeeResult;
import com.greenlink.core.application.company.usecase.ManageVendorFeeUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VendorFeeService implements ManageVendorFeeUseCase {

    private final CompanyRepository companyRepository;

    public VendorFeeService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public PageResult<VendorFeeResult> getVendorFees(String keyword, Pageable pageable) {
        return PageResult.of(companyRepository.searchVendorCompanies(keyword, pageable).map(VendorFeeResult::from));
    }

    @Override
    @Transactional
    public VendorFeeResult updateVendorFee(Long companyId, long vendorFee) {
        Company company = companyRepository.findById(companyId)
                .filter(c -> c.getType() == CompanyType.VENDOR || c.getType() == CompanyType.BOTH)
                .orElseThrow(() -> new BusinessException(VendorErrorCode.VENDOR_NOT_FOUND));
        company.changeVendorFee(vendorFee);
        return VendorFeeResult.from(companyRepository.save(company));
    }
}
