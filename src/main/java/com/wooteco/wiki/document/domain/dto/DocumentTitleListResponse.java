package com.wooteco.wiki.document.domain.dto;

import com.wooteco.wiki.document.domain.DocumentType;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentTitleListResponse(
        String title,
        UUID uuid,
        DocumentType documentType,
        LocalDateTime generateTime
) {
}