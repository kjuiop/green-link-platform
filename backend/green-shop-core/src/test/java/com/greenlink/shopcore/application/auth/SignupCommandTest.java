package com.greenlink.shopcore.application.auth;

import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignupCommandTest {

    @Test
    void toString에_평문_비밀번호가_포함되지_않는다() {
        SignupCommand cmd = new SignupCommand("shop01", "Passw0rd!", "홍길동", "010-1234-5678", null,
            "123-45-67890", "꽃집", null, null);

        assertThat(cmd.toString()).doesNotContain("Passw0rd!").contains("shop01");
    }
}
