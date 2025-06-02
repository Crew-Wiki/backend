package com.wooteco.wiki.global.auth.service;

import com.wooteco.wiki.admin.domain.Admin;
import com.wooteco.wiki.admin.domain.dto.AdminResponse;
import com.wooteco.wiki.admin.domain.dto.LoginRequest;
import com.wooteco.wiki.global.auth.domain.dto.TokenInfoDto;
import com.wooteco.wiki.global.auth.domain.dto.TokenResponse;
import com.wooteco.wiki.admin.exception.NotFoundAdminException;
import com.wooteco.wiki.global.auth.exception.WrongTokenException;
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
                .orElseThrow(NotFoundAdminException::new);
        return createToken(TokenInfoDto.of(admin));
    }

    public TokenResponse createToken(TokenInfoDto tokenInfoDto) {
        String accessToken = jwtTokenProvider.createToken(tokenInfoDto);
        return new TokenResponse(accessToken);
    }

    public AdminResponse findMemberByToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new WrongTokenException();
        }
        String payload = jwtTokenProvider.getPayload(token);
        Admin admin = findAdmin(payload);
        return AdminResponse.of(admin);
    }

    public Admin findAdmin(String payload) {
        Long id = Long.valueOf(payload);
        return adminRepository.findById(id)
                .orElseThrow(NotFoundAdminException::new);
    }
}
