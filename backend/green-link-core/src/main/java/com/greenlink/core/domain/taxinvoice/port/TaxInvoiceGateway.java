package com.greenlink.core.domain.taxinvoice.port;

import java.time.LocalDate;
import java.util.Optional;

public interface TaxInvoiceGateway {

    record Account(String apiKey, String corpNum, String userId) {}

    record Party(String corpNum, String companyName, String ceoName, String address, String email) {}

    record IssueRequest(Party supplier, Party recipient, String mgtNum, LocalDate writeDate,
                        String itemName, long supplyAmount) {}

    record IssueResult(boolean success, String docKey, String failReason) {}

    /** 면세 전자계산서를 등록하고 즉시 발행한다. 세액은 0이다. */
    IssueResult issue(Account account, IssueRequest request);

    /** 관리번호로 이미 발행된 계산서가 있으면 문서 키를 반환한다. 조회할 수 없으면 예외. */
    Optional<String> findIssuedDocKey(Account account, String mgtNum);
}
