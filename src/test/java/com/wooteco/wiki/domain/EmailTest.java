package com.wooteco.wiki.domain;

import com.wooteco.wiki.exception.WrongEmailException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {
    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "a@b", "A#d.com"})
    @DisplayName("이메일 형식이 아닌 경우 예외가 발생하는지 확인")
    void failCauseWrongEmail(String rawEmail) {
        Assertions.assertThatThrownBy(() -> new Email(rawEmail))
                .isInstanceOf(WrongEmailException.class);
    }
}
