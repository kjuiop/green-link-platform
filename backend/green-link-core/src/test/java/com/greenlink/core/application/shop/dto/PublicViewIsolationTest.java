package com.greenlink.core.application.shop.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublicViewIsolationTest {

    private static final List<String> FORBIDDEN = List.of("id", "companyId", "shopId", "published", "sortOrder",
            "ordererPhone", "ordererName", "recipientName", "recipientPhone", "address", "memo", "linkedOrderId");

    private static List<String> names(Class<? extends Record> type) {
        return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
    }

    @Test
    void 공개_쇼핑몰_뷰는_내부_식별자를_노출하지_않는다() {
        assertThat(names(PublicShopView.class)).doesNotContainAnyElementsOf(FORBIDDEN);
    }

    @Test
    void 공개_상품_뷰는_회사_식별자와_정렬값을_노출하지_않는다() {
        assertThat(names(PublicProductView.class)).doesNotContain("companyId", "shopId", "sortOrder", "visible");
    }

    @Test
    void 주문조회_뷰는_고객_연락처와_내부_ID를_노출하지_않는다() {
        assertThat(names(PublicOrderStatusView.class)).doesNotContainAnyElementsOf(FORBIDDEN);
        assertThat(names(PlacedShopOrderResult.class)).doesNotContainAnyElementsOf(FORBIDDEN);
    }
}
