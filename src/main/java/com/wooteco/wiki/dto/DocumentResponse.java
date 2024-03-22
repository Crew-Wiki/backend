package com.wooteco.wiki.dto;

import java.time.LocalDateTime;

public record DocumentResponse(long documentId, String title, String contents, String writer,
                               LocalDateTime generateTime) {
}
