package com.wooteco.wiki.graph.repository;

import java.util.UUID;

public record CrewGraphNodeReadModel(
        UUID documentUuid,
        String title
) {
}
