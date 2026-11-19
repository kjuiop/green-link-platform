package com.greenlink.admin.adapter.out.coupang;

import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class CoupangApiClientParseTest {

    @Test
    void 발주서_JSON을_도메인으로_변환한다() {
        String json = """
                {"shipmentBoxId":200,"orderId":100,"orderedAt":"2026-11-19T10:00:00","status":"ACCEPT",
                 "orderer":{"name":"홍길동","safeNumber":"0504-1111-2222","ordererNumber":null},
                 "receiver":{"name":"김철수","safeNumber":"","receiverNumber":"010-1234-5678","addr1":"서울시","addr2":"101호"},
                 "parcelPrintMessage":"문 앞",
                 "orderItems":[{"vendorItemName":"근조화환","shippingCount":2,"salesPrice":50000,"orderPrice":100000}]}
                """;
        CoupangApiClient client = new CoupangApiClient(null, "http://localhost");

        CoupangOrderSheet sheet = client.parseSheet(JsonMapper.builder().build().readTree(json));

        assertThat(sheet.orderId()).isEqualTo(100);
        assertThat(sheet.shipmentBoxId()).isEqualTo(200);
        assertThat(sheet.ordererPhone()).isEqualTo("0504-1111-2222");
        assertThat(sheet.receiverPhone()).isEqualTo("010-1234-5678");
        assertThat(sheet.receiverAddressDetail()).isEqualTo("101호");
        assertThat(sheet.deliveryMessage()).isEqualTo("문 앞");
        assertThat(sheet.totalPrice()).isEqualTo(100000);
        assertThat(sheet.items()).hasSize(1);
        assertThat(sheet.items().get(0).quantity()).isEqualTo(2);
        assertThat(sheet.orderedAt()).isNotNull();
    }
}
