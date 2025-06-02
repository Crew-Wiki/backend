package com.wooteco.wiki.service;

import com.wooteco.wiki.admin.domain.Admin;
import com.wooteco.wiki.global.auth.service.AuthService;
import com.wooteco.wiki.admin.domain.dto.LoginRequest;
import com.wooteco.wiki.global.auth.domain.dto.TokenInfoDto;
import com.wooteco.wiki.global.auth.domain.dto.TokenResponse;
import com.wooteco.wiki.admin.exception.NotFoundAdminException;
import com.wooteco.wiki.global.auth.JwtTokenProvider;
import com.wooteco.wiki.admin.repository.AdminRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        AuthService.class,
        JwtTokenProvider.class,
})
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private AdminRepository adminRepository;

    @Nested
    @DisplayName("TokenInfoDto로 Jwt 토큰 생성")
    class createToken {

        @DisplayName("TokenInfoDto로 Jwt 토큰 생성된다.")
        @Test
        void createToken_success() {
            // given
            TokenInfoDto tokenInfoDto = new TokenInfoDto(1L);

            // when
            TokenResponse tokenResponse = authService.createToken(tokenInfoDto);

            // then
            Assertions.assertThat(tokenResponse).isNotNull();
            System.out.println(tokenResponse);
        }
    }

    @Nested
    @DisplayName("loginRequest로 Jwt 토큰 생성")
    class login {

        @DisplayName("존재하는 어드민 정보로 요청했을 시 Jwt 토큰을 반환한다.")
        @Test
        void login_success_byExistingAdmin() {
            // given
            Admin savedAdmin = adminRepository.save(new Admin("admin", "password"));
            LoginRequest loginRequest = new LoginRequest(savedAdmin.getLoginId(), savedAdmin.getPassword());

            // when
            TokenResponse tokenResponse = authService.login(loginRequest);

            // then
            Assertions.assertThat(tokenResponse).isNotNull();
            System.out.println(tokenResponse);
        }

        @DisplayName("존재하지 않는 어드민 정보로 요쳥했을 때 예외 발생한다. : NotFoundAdminException")
        @Test
        void login_throwException_byInValidAdmin() {
            // given
            LoginRequest loginRequest = new LoginRequest("invalidLoginId", "invalidPassword");

            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> authService.login(loginRequest)
            ).isInstanceOf(NotFoundAdminException.class);
        }
    }
}
