package com.wooteco.wiki.global.auth.service;

import com.wooteco.wiki.admin.domain.Admin;
import com.wooteco.wiki.admin.domain.dto.AdminResponse;
import com.wooteco.wiki.admin.domain.dto.LoginRequest;
import com.wooteco.wiki.global.auth.Role;
import com.wooteco.wiki.global.auth.domain.dto.TokenInfoResponse;
import com.wooteco.wiki.global.auth.domain.dto.TokenResponse;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.global.auth.JwtTokenProvider;
import com.wooteco.wiki.admin.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminRepository adminRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, AdminRepository adminRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminRepository = adminRepository;
    }

    public TokenResponse login(LoginRequest loginRequest) {
        Admin admin = adminRepository.findOneByLoginIdAndPassword(loginRequest.loginId(), loginRequest.password())
                .orElseThrow(() -> new WikiException(ErrorCode.ADMIN_NOT_FOUND));
        return createToken(TokenInfoResponse.of(admin, Role.ROLE_ADMIN));
    }

    public TokenResponse createToken(TokenInfoResponse tokenInfoDto) {
        String accessToken = jwtTokenProvider.createToken(tokenInfoDto);
        return new TokenResponse(accessToken);
    }

    public AdminResponse findMemberByToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new WikiException(ErrorCode.WRONG_TOKEN);
        }
        String payload = jwtTokenProvider.getPayload(token);
        Admin admin = findAdmin(payload);
        return AdminResponse.of(admin);
    }

    public Admin findAdmin(String payload) {
        Long id = Long.valueOf(payload);
        return adminRepository.findById(id)
                .orElseThrow(() -> new WikiException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
