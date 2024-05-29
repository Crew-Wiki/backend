package com.wooteco.wiki.domain;

import com.wooteco.wiki.exception.WrongPasswordException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {
    @ParameterizedTest
    @ValueSource(strings = {"1234", "1234a", "qwer1@", "123456A", "01234567890123456789a"})
    @DisplayName("비밀번호 정책에 맞지 않는 것이 생성되지 않는지 확인")
    void failWhenWrongPassword(String password) {
        Assertions.assertThatThrownBy(() -> new Password(password))
                .isInstanceOf(WrongPasswordException.class);
    }
}
