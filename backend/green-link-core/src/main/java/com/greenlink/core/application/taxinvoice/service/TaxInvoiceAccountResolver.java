package com.greenlink.core.application.taxinvoice.service;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Account;
import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway.Party;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TaxInvoiceAccountResolver {

    private final SmsSettingsRepository smsSettingsRepository;
    private final CompanyRepository companyRepository;

    public TaxInvoiceAccountResolver(SmsSettingsRepository smsSettingsRepository, CompanyRepository companyRepository) {
        this.smsSettingsRepository = smsSettingsRepository;
        this.companyRepository = companyRepository;
    }

    public Optional<Account> resolve() {
        SmsSettings s = smsSettingsRepository.get();
        if (!"BAROBILL".equalsIgnoreCase(s.getProvider())
                || blank(s.getApiKey()) || blank(s.getCorpNum()) || blank(s.getUserId())) {
            return Optional.empty();
        }
        return Optional.of(new Account(s.getApiKey(), s.getCorpNum(), s.getUserId()));
    }

    /** 공급자(온우림) 정보. 시스템 바로빌 계정의 사업자번호와 같은 회사가 등록되어 있으면 그 정보를 쓴다. */
    public Party supplier(Account account) {
        String digits = digits(account.corpNum());
        Company company = companyRepository.findByBusinessNo(digits)
                .or(() -> companyRepository.findByBusinessNo(format(digits)))
                .orElse(null);
        if (company == null) {
            return new Party(digits, null, null, null, null);
        }
        return new Party(digits, company.getCompanyName(), company.getCeoName(),
                join(company.getAddress(), company.getDetailAddress()), company.getTaxInvoiceEmail());
    }

    static String digits(String s) {
        return s == null ? "" : s.replaceAll("[^0-9]", "");
    }

    static String join(String address, String detail) {
        String a = address == null ? "" : address.trim();
        String d = detail == null ? "" : detail.trim();
        return (a + " " + d).trim();
    }

    private static String format(String d) {
        return d.length() == 10 ? d.substring(0, 3) + "-" + d.substring(3, 5) + "-" + d.substring(5) : d;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
