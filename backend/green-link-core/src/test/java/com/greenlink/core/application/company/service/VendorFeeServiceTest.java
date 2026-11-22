package com.greenlink.core.application.company.service;

import com.greenlink.core.application.company.dto.VendorFeeResult;
import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VendorFeeServiceTest {

    @Mock CompanyRepository companyRepository;
    @InjectMocks VendorFeeService service;

    private Company company(CompanyType type) {
        return Company.of(1L, "000-00-00001", "수주사", null, "02-000-0001", null, null, null,
                type, CompanyStatus.ACTIVE, true, null, null, null, null, null, BusinessStatus.OPEN);
    }

    @Test
    void 수주사_목록을_수수료와_함께_조회한다() {
        PageRequest pageable = PageRequest.of(0, 20);
        given(companyRepository.searchVendorCompanies("수주", pageable))
                .willReturn(new PageImpl<>(List.of(company(CompanyType.VENDOR)), pageable, 1));

        var page = service.getVendorFees("수주", pageable);

        assertThat(page.content()).extracting(VendorFeeResult::vendorFee).containsExactly(3000L);
    }

    @Test
    void 수수료를_수정한다() {
        given(companyRepository.findById(1L)).willReturn(Optional.of(company(CompanyType.BOTH)));
        given(companyRepository.save(any(Company.class))).willAnswer(inv -> inv.getArgument(0));

        VendorFeeResult result = service.updateVendorFee(1L, 5000L);

        assertThat(result.vendorFee()).isEqualTo(5000L);
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void 발주사_전용_회사는_수정할_수_없다() {
        given(companyRepository.findById(1L)).willReturn(Optional.of(company(CompanyType.ORDERING)));

        assertThatThrownBy(() -> service.updateVendorFee(1L, 5000L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(VendorErrorCode.VENDOR_NOT_FOUND));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_회사는_404다() {
        given(companyRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateVendorFee(1L, 5000L)).isInstanceOf(BusinessException.class);
    }
}
