package com.wooteco.wiki.document.domain.dto;

import com.wooteco.wiki.document.domain.DocumentType;
import java.util.UUID;

public record DocumentSearchResponse(
        String title,
        UUID uuid,
        DocumentType documentType
) {
}

