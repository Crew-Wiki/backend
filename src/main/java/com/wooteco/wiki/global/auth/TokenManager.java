//package com.wooteco.wiki.domain;
//
//import com.wooteco.wiki.config.auth.exception.WrongTokenException;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jws;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.Date;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TokenManager {
//    private static final String MEMBER_ID = "member_id";
//    private static final String TOKEN_TYPE = "token_type";
//    private static final long ACCESS_TOKEN_LIFE_TIME_AS_HOUR = 1;
//    private static final long REFRESH_TOKEN_LIFE_TIME_AS_HOUR = 24;
//    @Value("${jwt.key}")
//    private String secretKey;
//
//    /**
//     * 생성된 엑세스 토큰은 리프레쉬 토큰으로 사용할 수 없어야 함. 엑세스 토큰에는 회원의 식별자를 제외한 다른 개인 정보가 포함되면 안됨. 우선 엑세스 토큰 유효 시간은 발급 시점으로부터 1시간으로 설정함.
//     * 논의 후 조정하기로!
//     *
//     * @param member 회원 도메인
//     * @return 엑세스 토큰
//     */
//    public String generateAccessToken(Member member) {
//        LocalDateTime rawExpiredTime = LocalDateTime.now().plusHours(ACCESS_TOKEN_LIFE_TIME_AS_HOUR);
//        Date expiredTime = localDateTimeToDate(rawExpiredTime);
//        return Jwts.builder()
//                .expiration(expiredTime)
//                .claim(TOKEN_TYPE, "access")
//                .claim(MEMBER_ID, member.getMemberId())
//                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
//                .compact();
//    }
//
//    private Date localDateTimeToDate(LocalDateTime expiredTime) {
//        Instant instant = expiredTime.atZone(ZoneId.systemDefault()).toInstant();
//        return Date.from(instant);
//    }
//
//    /**
//     * 생성된 리프레쉬 토큰은 엑세스 토큰으로 사용할 수 없어야 함. 우선 리프레쉬 토큰 유효 시간은 발급 시점으로부터 1일로 설정함. 논의 후 조정하기로!
//     *
//     * @param member 회원 도메인
//     * @return 리프레쉬 토큰
//     */
//    public String generateRefreshToken(Member member) {
//        LocalDateTime rawExpiredTime = LocalDateTime.now().plusHours(REFRESH_TOKEN_LIFE_TIME_AS_HOUR);
//        Date expiredTime = localDateTimeToDate(rawExpiredTime);
//        return Jwts.builder()
//                .expiration(expiredTime)
//                .claim(TOKEN_TYPE, "refresh")
//                .claim(MEMBER_ID, member.getMemberId())
//                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
//                .compact();
//    }
//
//    /**
//     * 회원 정보로부터 회원의 식별자를 알아냄.
//     *
//     * @param accessToken 엑세스 토큰
//     * @return 회원의 식별자.
//     * @throws WrongTokenException 엑세스 토큰에 문제(잘못된 토큰, 기간 만료 등)가 있어 회원 식별자를 추출할 수 없는 경우
//     */
//    public long extractMemberId(String accessToken) throws WrongTokenException {
//        try {
//            Jws<Claims> claimsJws = Jwts.parser()
//                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
//                    .build()
//                    .parseSignedClaims(accessToken);
//
//            Claims payload = claimsJws.getPayload();
//            validateTokenIsAccessToken(payload);
//            return payload.get(MEMBER_ID, Long.class);
//        } catch (JwtException | IllegalArgumentException e) {
//            throw new WrongTokenException("잘못된 토큰입니다.");
//        }
//    }
//
//    private static void validateTokenIsAccessToken(Claims payload) {
//        String tokenType = payload.get(TOKEN_TYPE, String.class);
//        if (!tokenType.equals("access")) {
//            throw new WrongTokenException("엑세스 토큰이 아닙니다.");
//        }
//    }
//}
