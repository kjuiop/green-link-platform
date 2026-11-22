package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.taxinvoice.port.TaxInvoiceGateway;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** 필드명·코드값은 바로빌 TI 문서 대조 전 가정값이다. */
@Component
public class BarobillTaxInvoiceClient implements TaxInvoiceGateway {

    private static final String ISSUE_METHOD = "RegistAndIssueTaxInvoice";
    private static final String STATE_METHOD = "GetTaxInvoiceStateEX";
    private static final int MIN_ISSUED_STATE = 3011;
    private static final DateTimeFormatter WRITE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BarobillProperties props;
    private final RestTemplate restTemplate;

    public BarobillTaxInvoiceClient(BarobillProperties props, RestTemplate smsRestTemplate) {
        this.props = props;
        this.restTemplate = smsRestTemplate;
    }

    @Override
    public IssueResult issue(Account account, IssueRequest request) {
        try {
            String inner = BarobillSoap.element("CERTKEY", account.apiKey())
                    + BarobillSoap.element("CorpNum", digits(account.corpNum()))
                    + "<Invoice>" + invoiceXml(account, request) + "</Invoice>"
                    + BarobillSoap.element("SendSMS", "false")
                    + BarobillSoap.element("ForceIssue", "false")
                    + BarobillSoap.element("MailTitle", "");
            String result = call(ISSUE_METHOD, inner)
                    .flatMap(r -> BarobillSoap.parseResult(r, ISSUE_METHOD)).orElse("");
            if (result.isEmpty()) return new IssueResult(false, null, "바로빌 응답이 올바르지 않습니다.");
            if (BarobillSoap.isErrorCode(result)) return new IssueResult(false, null, "바로빌 오류 (" + result + ")");
            if (!"1".equals(result)) return new IssueResult(false, null, "바로빌 응답을 해석할 수 없습니다. (" + result + ")");
            return new IssueResult(true, request.mgtNum(), null);
        } catch (HttpClientErrorException e) {
            return new IssueResult(false, null, "바로빌 응답 오류 (HTTP " + e.getStatusCode().value() + ")");
        }
    }

    @Override
    public Optional<String> findIssuedDocKey(Account account, String mgtNum) {
        String inner = BarobillSoap.element("CERTKEY", account.apiKey())
                + BarobillSoap.element("CorpNum", digits(account.corpNum()))
                + BarobillSoap.element("MgtKey", mgtNum);
        String xml = call(STATE_METHOD, inner).orElseThrow(() -> new IllegalStateException("empty response"));
        return parseIssuedState(xml) ? Optional.of(mgtNum) : Optional.empty();
    }

    static boolean parseIssuedState(String xml) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("<(?:\\w+:)?BarobillState[^>]*>\\s*(-?\\d+)\\s*</(?:\\w+:)?BarobillState>").matcher(xml == null ? "" : xml);
        if (!m.find()) return false;
        return Integer.parseInt(m.group(1)) >= MIN_ISSUED_STATE;
    }

    static String invoiceXml(Account account, IssueRequest r) {
        long amount = r.supplyAmount();
        return BarobillSoap.element("IssueDirection", "1")
                + BarobillSoap.element("TaxInvoiceType", "2")
                + BarobillSoap.element("ModifyCode", "")
                + BarobillSoap.element("TaxType", "3")
                + BarobillSoap.element("TaxCalcType", "1")
                + BarobillSoap.element("PurposeType", "2")
                + BarobillSoap.element("WriteDate", r.writeDate().format(WRITE_DATE))
                + BarobillSoap.element("AmountTotal", String.valueOf(amount))
                + BarobillSoap.element("TaxTotal", "0")
                + BarobillSoap.element("TotalAmount", String.valueOf(amount))
                + BarobillSoap.element("Cash", "0")
                + BarobillSoap.element("ChkBill", "0")
                + BarobillSoap.element("Credit", String.valueOf(amount))
                + "<InvoicerParty>" + partyXml(r.supplier(), r.mgtNum(), account.userId()) + "</InvoicerParty>"
                + "<InvoiceeParty>" + partyXml(r.recipient(), "", "") + "</InvoiceeParty>"
                + "<TaxInvoiceTradeLineItems><TaxInvoiceTradeLineItem>"
                + BarobillSoap.element("PurchaseExpiry", r.writeDate().format(WRITE_DATE))
                + BarobillSoap.element("Name", r.itemName())
                + BarobillSoap.element("Amount", String.valueOf(amount))
                + BarobillSoap.element("Tax", "0")
                + "</TaxInvoiceTradeLineItem></TaxInvoiceTradeLineItems>";
    }

    private static String partyXml(Party p, String mgtNum, String contactId) {
        return BarobillSoap.element("MgtNum", mgtNum)
                + BarobillSoap.element("CorpNum", digits(p.corpNum()))
                + BarobillSoap.element("CorpName", p.companyName())
                + BarobillSoap.element("CEOName", p.ceoName())
                + BarobillSoap.element("Addr", p.address())
                + BarobillSoap.element("ContactID", contactId)
                + BarobillSoap.element("Email", p.email());
    }

    private Optional<String> call(String method, String innerXml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));
        headers.add("SOAPAction", BarobillSoap.soapAction(method));
        byte[] response = restTemplate.exchange(props.taxInvoiceUrl(), HttpMethod.POST,
                new HttpEntity<>(BarobillSoap.buildEnvelopeXml(method, innerXml), headers), byte[].class).getBody();
        if (response == null) return Optional.empty();
        return Optional.of(new String(response, StandardCharsets.UTF_8));
    }

    private static String digits(String s) {
        return s == null ? "" : s.replaceAll("[^0-9]", "");
    }
}
