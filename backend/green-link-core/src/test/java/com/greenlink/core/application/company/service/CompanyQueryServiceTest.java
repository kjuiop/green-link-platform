package com.greenlink.core.application.company.service;

import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CompanyQueryServiceTest {

    @Mock CompanyRepository companyRepository;
    @InjectMocks CompanyQueryService service;

    private Company company(CompanyType type, boolean onurimEnabled) {
        Company company = Company.of(1L, "000-00-00001", "회사", null, null, null, null, null,
                type, CompanyStatus.ACTIVE, true, null, null, null, null, null, BusinessStatus.OPEN);
        company.restoreServiceFlags(onurimEnabled, !onurimEnabled);
        return company;
    }

    @Test
    void findByBusinessNo_온우림_회사는_조회된다() {
        given(companyRepository.findByBusinessNo("000-00-00001")).willReturn(Optional.of(company(CompanyType.VENDOR, true)));

        assertThat(service.findByBusinessNo("000-00-00001")).isPresent();
    }

    @Test
    void findByBusinessNo_온우림_미사용_회사는_숨긴다() {
        given(companyRepository.findByBusinessNo("000-00-00001")).willReturn(Optional.of(company(CompanyType.VENDOR, false)));

        assertThat(service.findByBusinessNo("000-00-00001")).isEmpty();
    }

    @Test
    void findByBusinessNo_타입이_없는_회사는_숨긴다() {
        given(companyRepository.findByBusinessNo("000-00-00001")).willReturn(Optional.of(company(null, true)));

        assertThat(service.findByBusinessNo("000-00-00001")).isEmpty();
    }
}
