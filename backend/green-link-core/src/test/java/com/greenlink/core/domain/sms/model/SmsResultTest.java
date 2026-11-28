package com.greenlink.core.domain.sms.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmsResultTest {

    @Test
    void reasonOf는_메시지가_있으면_메시지를_반환한다() {
        assertThat(SmsResult.reasonOf(new IllegalStateException("연결 거부"))).isEqualTo("연결 거부");
    }

    @Test
    void reasonOf는_메시지가_없거나_공백이면_예외_클래스명을_반환한다() {
        assertThat(SmsResult.reasonOf(new IllegalStateException())).isEqualTo("IllegalStateException");
        assertThat(SmsResult.reasonOf(new IllegalStateException(" "))).isEqualTo("IllegalStateException");
    }
}
