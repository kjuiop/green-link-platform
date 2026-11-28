package com.greenlink.core.domain.sms.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmsResultTest {

    @Test
    void reasonOf는_메시지가_있으면_메시지만_반환한다() {
        assertThat(SmsResult.reasonOf(new IllegalStateException("연결 거부"))).isEqualTo("연결 거부");
    }

    @Test
    void rejectionReason은_코드와_메시지를_함께_반환하고_메시지가_없으면_코드만_반환한다() {
        assertThat(SmsResult.rejectionReason("155", "발신번호 미등록")).isEqualTo("155: 발신번호 미등록");
        assertThat(SmsResult.rejectionReason("155", null)).isEqualTo("155");
        assertThat(SmsResult.rejectionReason("155", " ")).isEqualTo("155");
    }

    @Test
    void reasonOf는_메시지가_없거나_공백이면_클래스명과_발생_위치를_반환한다() {
        assertThat(SmsResult.reasonOf(new IllegalStateException())).startsWith("IllegalStateException (SmsResultTest.");
        assertThat(SmsResult.reasonOf(new IllegalStateException(" "))).startsWith("IllegalStateException (SmsResultTest.");
    }

    @Test
    void reasonOf는_프로젝트_코드_위치가_없으면_클래스명만_반환한다() {
        Exception e = new RuntimeException();
        e.setStackTrace(new StackTraceElement[]{new StackTraceElement("org.springframework.Foo", "bar", "Foo.java", 1)});
        assertThat(SmsResult.reasonOf(e)).isEqualTo("RuntimeException");
    }
}
