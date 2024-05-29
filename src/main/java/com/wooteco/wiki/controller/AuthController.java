package com.wooteco.wiki.controller;

import com.wooteco.wiki.dto.AuthTokens;
import com.wooteco.wiki.dto.JoinRequest;
import com.wooteco.wiki.dto.LoginRequest;
import com.wooteco.wiki.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public AuthTokens login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/join")
    public AuthTokens join(@RequestBody JoinRequest joinRequest) {
        return authService.join(joinRequest);
    }
}
