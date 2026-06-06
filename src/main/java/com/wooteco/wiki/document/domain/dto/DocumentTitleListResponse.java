package com.wooteco.wiki.document.domain.dto;

import java.util.UUID;

public record DocumentTitleListResponse(
        String title,
        UUID uuid
) {
}
