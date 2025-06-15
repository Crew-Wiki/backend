package com.wooteco.wiki.admin.controller;

import com.wooteco.wiki.admin.domain.dto.AdminResponse;
import com.wooteco.wiki.admin.domain.dto.LoginRequest;
import com.wooteco.wiki.global.auth.domain.dto.TokenResponse;
import com.wooteco.wiki.global.auth.service.AuthService;
import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final String TOKEN_NAME_FIELD = "token";
    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<ApiResponse.SuccessBody<Void>> login(@RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_NAME_FIELD, tokenResponse.accessToken())
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();
        return ApiResponseGenerator.success(HttpStatus.OK, "로그인이 완료되었습니다.");
    }

    @GetMapping("/login/check")
    public ApiResponse<ApiResponse.SuccessBody<AdminResponse>> checkAuth(
            @CookieValue(name = TOKEN_NAME_FIELD) String token) {
        AdminResponse adminResponse = authService.findMemberByToken(token);
        return ApiResponseGenerator.success(adminResponse, HttpStatus.OK, "인증이 확인되었습니다.");
    }

    @PostMapping("/logout")
    public ApiResponse<ApiResponse.SuccessBody<Void>> logout(@CookieValue(name = TOKEN_NAME_FIELD) String token) {
        authService.findMemberByToken(token);
        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_NAME_FIELD, "")
                .domain("localhost")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(0))
                .sameSite("Strict")
                .build();
        return ApiResponseGenerator.success(HttpStatus.OK, "로그아웃이 완료되었습니다.");
    }
}
