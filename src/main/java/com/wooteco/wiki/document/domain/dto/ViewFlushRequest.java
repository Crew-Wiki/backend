package com.wooteco.wiki.document.domain.dto;

import java.util.Map;
import java.util.UUID;

public record ViewFlushRequest(
        Map<UUID, Integer> views
) {
}

