package com.wooteco.wiki.global.config.interceptor;

import com.wooteco.wiki.global.auth.JwtTokenProvider;
import com.wooteco.wiki.global.auth.Role;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final String TOKEN_NAME_FILED = "token";

    private final JwtTokenProvider jwtTokenProvider;

    public LoginCheckInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String token = extractTokenFromCookie(request);

        if (token == null) {
            throw new WikiException(ErrorCode.UNAUTHORIZED);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new WikiException(ErrorCode.WRONG_TOKEN);
        }

        Claims claims = jwtTokenProvider.getClaims(token);
        String roleStr = (String) claims.get("role");

        if (!roleStr.equals(Role.ROLE_ADMIN.name())) {
            throw new WikiException(ErrorCode.FORBIDDEN);
        }
        return true;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (TOKEN_NAME_FILED.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
