package com.wooteco.wiki.graph.repository;

import java.util.UUID;

public record CrewGraphReadModel(
        UUID documentUuid,
        String title,
        String contents
) {
}
