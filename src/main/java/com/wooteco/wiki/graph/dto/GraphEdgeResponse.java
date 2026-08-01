package com.wooteco.wiki.graph.dto;

import java.util.UUID;

public record GraphEdgeResponse(
        UUID sourceDocumentUuid,
        UUID targetDocumentUuid,
        GraphEdgeType type
) {
}
