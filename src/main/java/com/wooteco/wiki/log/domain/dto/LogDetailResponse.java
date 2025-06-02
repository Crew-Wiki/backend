package com.wooteco.wiki.log.domain.dto;

import java.time.LocalDateTime;

public record LogDetailResponse(Long logId, String title, String contents, String writer, LocalDateTime generateTime) {
}
