package com.greenlink.core.domain.sms.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmsResultTest {

    @Test
    void reasonOf는_메시지와_예외_발생_위치를_함께_반환한다() {
        assertThat(SmsResult.reasonOf(new IllegalStateException("연결 거부")))
                .startsWith("연결 거부 (SmsResultTest.");
    }

    @Test
    void reasonOf는_메시지가_없거나_공백이면_예외_클래스명을_사용한다() {
        assertThat(SmsResult.reasonOf(new IllegalStateException())).startsWith("IllegalStateException (SmsResultTest.");
        assertThat(SmsResult.reasonOf(new IllegalStateException(" "))).startsWith("IllegalStateException (SmsResultTest.");
    }

    @Test
    void reasonOf는_프로젝트_코드_위치가_없으면_위치를_붙이지_않는다() {
        Exception e = new RuntimeException("외부 오류");
        e.setStackTrace(new StackTraceElement[]{new StackTraceElement("org.springframework.Foo", "bar", "Foo.java", 1)});
        assertThat(SmsResult.reasonOf(e)).isEqualTo("외부 오류");
    }
}
