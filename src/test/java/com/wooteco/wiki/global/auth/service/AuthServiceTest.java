package com.wooteco.wiki.global.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wooteco.wiki.admin.domain.Admin;
import com.wooteco.wiki.admin.domain.dto.LoginRequest;
import com.wooteco.wiki.admin.repository.AdminRepository;
import com.wooteco.wiki.global.auth.JwtTokenProvider;
import com.wooteco.wiki.global.auth.Role;
import com.wooteco.wiki.global.auth.domain.dto.TokenInfoResponse;
import com.wooteco.wiki.global.auth.domain.dto.TokenResponse;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
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
    @DisplayName("TokenInfoResponse로 Jwt 토큰 생성")
    class CreateToken {

        @DisplayName("TokenInfoResponse로 Jwt 토큰 생성된다.")
        @Test
        void createToken_success_byTokenInfoResponse() {
            // given
            TokenInfoResponse tokenInfoDto = new TokenInfoResponse(1L, Role.ROLE_ADMIN);

            // when
            TokenResponse tokenResponse = authService.createToken(tokenInfoDto);

            // then
            assertThat(tokenResponse).isNotNull();
            System.out.println(tokenResponse);
        }
    }

    @Nested
    @DisplayName("loginRequest로 Jwt 토큰 생성")
    class Login {

        @DisplayName("존재하는 어드민 정보로 요청했을 시 Jwt 토큰을 반환한다.")
        @Test
        void login_success_byExistingAdmin() {
            // given
            Admin savedAdmin = adminRepository.save(new Admin("admin", "password"));
            LoginRequest loginRequest = new LoginRequest(savedAdmin.getLoginId(), savedAdmin.getPassword());

            // when
            TokenResponse tokenResponse = authService.login(loginRequest);

            // then
            assertThat(tokenResponse).isNotNull();
            System.out.println(tokenResponse);
        }

        @DisplayName("존재하지 않는 어드민 정보로 요청했을 때 예외 발생한다. : WikiException.ADMIN_NOT_FOUND")
        @Test
        void login_fail_byInvalidAdmin() {
            // given
            LoginRequest loginRequest = new LoginRequest("invalidLoginId", "invalidPassword");

            // then
            WikiException ex = assertThrows(WikiException.class,
                    () -> authService.login(loginRequest));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND);
        }
    }
}
