package com.wooteco.wiki.document.domain.dto;

import java.util.UUID;

public record DocumentUpdateRequest(
        String title,
        String contents,
        String writer,
        Long documentBytes,
        UUID uuid
) {
}

