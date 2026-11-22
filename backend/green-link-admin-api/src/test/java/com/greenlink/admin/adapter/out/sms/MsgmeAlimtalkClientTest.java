package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.model.AlimtalkRequest.AlimtalkVariables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MsgmeAlimtalkClientTest {

    @Test
    void 변수는_정해진_순서로_파이프_구분해_조립한다() {
        String variable = MsgmeAlimtalkClient.toVariable(
                new AlimtalkVariables("온우림", "홍길동", "ORD-1", "서울병원", "tok-123"));

        assertThat(variable).isEqualTo("온우림|홍길동|ORD-1|서울병원|tok-123");
    }

    @Test
    void 값의_파이프는_제거하고_null은_빈_문자열로_처리한다() {
        String variable = MsgmeAlimtalkClient.toVariable(
                new AlimtalkVariables("온|우림", null, "ORD-1", null, "tok"));

        assertThat(variable).isEqualTo("온우림||ORD-1||tok");
    }
}
