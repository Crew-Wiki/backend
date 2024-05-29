package com.wooteco.wiki.domain;

import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MemberTest {

    public static Stream<Arguments> updateEmailAndPasswordWhenINITIALParameters() {
        return Stream.of(
                Arguments.of(MemberState.INITIAL, true),
                Arguments.of(MemberState.WAITING, false),
                Arguments.of(MemberState.ALLOWED, false)
        );
    }

    @ParameterizedTest
    @MethodSource("updateEmailAndPasswordWhenINITIALParameters")
    @DisplayName("상태가 INITIAL 인 경우에만 회원의 이메일과 비밀번호가 업데이트 되는지 확인")
    void updateEmailAndPasswordWhenINITIAL(MemberState memberState, boolean isChanged) {
        Member member = Member.builder()
                .memberId(1L)
                .email("email@email.com")
                .password("qwer1234!")
                .nickname("nickname")
                .state(memberState)
                .build();

        String changedEmail = "change@chnage.com";
        member.updateEmailAndPasswordWhenINITIAL(changedEmail, "qwer1234!!");

        Assertions.assertThat(member.getRawEmail().equals(changedEmail))
                .isEqualTo(isChanged);
    }
}
