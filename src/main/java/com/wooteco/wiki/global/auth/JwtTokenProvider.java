package com.wooteco.wiki.global.auth;

import com.wooteco.wiki.global.auth.domain.dto.TokenInfoDto;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String strSecretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        this.secretKey = Keys.hmacShaKeyFor(strSecretKey.getBytes());
    }

    public String createToken(TokenInfoDto tokenInfoDto) {

        Map<String, ?> claims = getMyClaimMap(tokenInfoDto);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        try {
            return Jwts.builder()
                    .subject(String.valueOf(tokenInfoDto.id()))
                    .claims(claims)
                    .issuedAt(now)
                    .expiration(validity)
                    .signWith(secretKey)
                    .compact();
        } catch (JwtException e) {
            throw new WikiException(ErrorCode.TOKEN_CREATE_ERROR);
        }
    }

    private Map<String, ?> getMyClaimMap(TokenInfoDto tokenInfoDto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tokenInfoDto.id());
        map.put("role", tokenInfoDto.role());

        return new HashMap<>(map);
    }

    public String getPayload(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
