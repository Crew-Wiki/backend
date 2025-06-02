package com.wooteco.wiki.controller;

import com.wooteco.wiki.admin.domain.Admin;
import com.wooteco.wiki.admin.domain.dto.LoginRequest;
import com.wooteco.wiki.global.auth.domain.dto.TokenResponse;
import com.wooteco.wiki.admin.repository.AdminRepository;
import com.wooteco.wiki.global.auth.service.AuthService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    private static final String TOKEN_NAME_FILED = "token";

    @Autowired
    private AuthService authService;
    @Autowired
    private AdminRepository adminRepository;

    private Admin savedAdmin;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        savedAdmin = adminRepository.save(new Admin("admin", "password"));
    }

    @Nested
    @DisplayName("로그인 기능")
    class login {

        @DisplayName("유효한 이메일과 비밀번호로 로그인을 성공한다.")
        @Test
        void login_success() {
            // given

            // when
            LoginRequest requestDto = new LoginRequest(savedAdmin.getLoginId(), savedAdmin.getPassword());

            // then
            RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestDto)
                    .when().post("/admin/login")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().response();
        }

        @DisplayName("유효하지 않는 이메일과 비밀번호로 로그인을 시도하여 실패: 상태 코드 404를 반환한다.")
        @Test
        void login_throwException_byInvalidEmail() {
            // given
            // when
            LoginRequest requestDto = new LoginRequest("invalidLoginId", "invalidPassword");

            // then
            RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestDto)
                    .when().post("/admin/login")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().response();
        }
    }

    @Nested
    @DisplayName("토큰 기반 어드민 조회 API 테스트")
    class checkAuth {

        @DisplayName("토큰으로 어드민 조회 시 200 OK 반환")
        @Test
        void checkAuth_success() {
            // given
            String expectedLoginId = "loginIdCS";
            Admin savedAdmin = adminRepository.save(new Admin(expectedLoginId, "password"));
            LoginRequest loginRequest = new LoginRequest(savedAdmin.getLoginId(), savedAdmin.getPassword());
            TokenResponse responseDto = authService.login(loginRequest);
            String token = responseDto.accessToken();

            // when
            // then
            RestAssured
                    .given().log().all()
                    .cookie(TOKEN_NAME_FILED, token)
                    .when().get("/admin/login/check")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());
        }
    }
}
