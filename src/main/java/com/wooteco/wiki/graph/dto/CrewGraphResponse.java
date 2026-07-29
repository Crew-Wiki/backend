package com.wooteco.wiki.graph.dto;

import java.util.List;

public record CrewGraphResponse(
        List<GraphNodeResponse> nodes,
        List<GraphEdgeResponse> edges
) {

    public CrewGraphResponse {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }

    public static CrewGraphResponse of(
            List<GraphNodeResponse> nodes,
            List<GraphEdgeResponse> edges
    ) {
        return new CrewGraphResponse(nodes, edges);
    }
}
