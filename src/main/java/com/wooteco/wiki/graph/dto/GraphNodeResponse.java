package com.wooteco.wiki.graph.dto;

import java.util.UUID;

public record GraphNodeResponse(
        UUID documentUuid,
        String title,
        GraphNodeType type
) {

    public static GraphNodeResponse fromCrewDocument(
            UUID documentUuid,
            String title
    ) {
        return new GraphNodeResponse(
                documentUuid,
                title,
                GraphNodeType.CREW
        );
    }
}
