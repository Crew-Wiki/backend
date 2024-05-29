package com.wooteco.wiki.service;

import com.wooteco.wiki.domain.TokenManager;
import com.wooteco.wiki.dto.AuthTokens;
import com.wooteco.wiki.dto.JoinRequest;
import com.wooteco.wiki.dto.LoginRequest;
import com.wooteco.wiki.exception.DuplicateEmailException;
import com.wooteco.wiki.exception.MemberNotFoundException;
import com.wooteco.wiki.testinfra.ActiveProfileSpringBootTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class AuthServiceTest extends ActiveProfileSpringBootTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("로그인 성공 시 엑세스 토큰이 잘 생성되는지 확인")
    void login() {
        authService.join(new JoinRequest("email@email.com", "nickname", "qwer1234!"));

        AuthTokens authTokens = authService.login(new LoginRequest("email@email.com", "qwer1234!"));

        long extractMemberId = tokenManager.extractMemberId(authTokens.accessToken());

        Assertions.assertThat(extractMemberId)
                .isEqualTo(1);
    }

    @Test
    @DisplayName("이메일 혹은 비밀번호가 잘못되어 로그인 실패시 예외가 발생하는지 확인")
    void loginFailWhenWrongAuthInfo() {
        Assertions.assertThatThrownBy(() -> authService.login(new LoginRequest("email@email.com", "qwer1234!")))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("회원가입 시 엑세스 토큰이 잘 생성되는지 확인")
    void join() {
        AuthTokens authTokens = authService.join(new JoinRequest("email@email.com", "nickname", "qwer1234!"));

        long extractMemberId = tokenManager.extractMemberId(authTokens.accessToken());

        Assertions.assertThat(extractMemberId)
                .isEqualTo(1);
    }

    @Test
    @DisplayName("미리 생성해둔 닉네임의 회원으로 회원가입 시도 시 회원가입이 잘 되는지 확인")
    void joinWithInitial() {
        String sql = "INSERT INTO member (nickname, email, password, state) VALUES ('켈리', 'kelly@example.com', 'qwer1234!', 'INITIAL')";
        jdbcTemplate.update(sql);

        AuthTokens authTokens = authService.join(new JoinRequest("kelly@example.com", "켈리", "qwer1234!"));

        long extractMemberId = tokenManager.extractMemberId(authTokens.accessToken());

        Assertions.assertThat(extractMemberId)
                .isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"WAITING", "ALLOWED"})
    @DisplayName("이미 있는 이메일로 회원가입 시도 시 예외가 발생하는지 확인")
    void joinFailWhenDuplicate(String memberState) {
        String sql = "INSERT INTO member (nickname, email, password, state) VALUES ('켈리', 'kelly@example.com', 'qwer1234!', '%s')"
                .formatted(memberState);
        jdbcTemplate.update(sql);

        Assertions.assertThatThrownBy(() -> authService.join(new JoinRequest("kelly@example.com", "켈리", "qwer1234!")))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
