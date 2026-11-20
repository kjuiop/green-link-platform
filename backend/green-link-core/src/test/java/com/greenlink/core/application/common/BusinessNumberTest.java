package com.greenlink.core.application.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessNumberTest {

    @Test
    void 하이픈과_공백을_제거해_숫자만_남긴다() {
        assertThat(BusinessNumber.normalize("476-86-02607")).isEqualTo("4768602607");
        assertThat(BusinessNumber.normalize(" 476 86 02607 ")).isEqualTo("4768602607");
        assertThat(BusinessNumber.normalize("4768602607")).isEqualTo("4768602607");
    }

    @Test
    void null과_빈값은_그대로_반환한다() {
        assertThat(BusinessNumber.normalize(null)).isNull();
        assertThat(BusinessNumber.normalize("")).isEmpty();
        assertThat(BusinessNumber.normalize("  ")).isEqualTo("  ");
    }
}
