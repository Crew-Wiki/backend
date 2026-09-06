package com.wooteco.wiki.graph.dto;

import java.util.List;

public record CrewGraphElements(
        List<GraphNodeResponse> nodes,
        List<GraphEdgeResponse> edges
) {

    public CrewGraphElements {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }

    public static CrewGraphElements of(
            List<GraphNodeResponse> nodes,
            List<GraphEdgeResponse> edges
    ) {
        return new CrewGraphElements(nodes, edges);
    }
}
