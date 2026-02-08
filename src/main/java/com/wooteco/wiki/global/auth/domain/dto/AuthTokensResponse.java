package com.wooteco.wiki.global.auth.domain.dto;

public record AuthTokensResponse(String accessToken, String refreshToken) {
}
