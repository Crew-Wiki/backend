package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.graph.dto.GraphEdgeResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeType;
import com.wooteco.wiki.graph.repository.DocumentReferenceReadModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

// 두 읽기 경로가 같은 edge를 만들도록 정규화 규칙을 한곳에 모은다.
@Component
public class CrewGraphEdgeNormalizer {

    // 방향성 참조를 UUID가 작은 문서를 source로 둔 무방향 edge 하나로 합치고, 자기참조는 제외한다.
    public List<GraphEdgeResponse> normalize(List<DocumentReferenceReadModel> referenceReadModels) {
        Set<GraphEdgeResponse> edges = new LinkedHashSet<>();
        for (DocumentReferenceReadModel referenceReadModel : referenceReadModels) {
            addReferenceEdgeIfValid(referenceReadModel, edges);
        }
        List<GraphEdgeResponse> sortedEdges = new ArrayList<>(edges);
        sortedEdges.sort(Comparator
                .comparing(GraphEdgeResponse::sourceDocumentUuid)
                .thenComparing(GraphEdgeResponse::targetDocumentUuid));
        return List.copyOf(sortedEdges);
    }

    private void addReferenceEdgeIfValid(
            DocumentReferenceReadModel referenceReadModel,
            Set<GraphEdgeResponse> edges
    ) {
        UUID sourceDocumentUuid = referenceReadModel.sourceDocumentUuid();
        UUID targetDocumentUuid = referenceReadModel.targetDocumentUuid();
        if (sourceDocumentUuid.equals(targetDocumentUuid)) {
            return;
        }
        edges.add(createReferenceEdge(sourceDocumentUuid, targetDocumentUuid));
    }

    private GraphEdgeResponse createReferenceEdge(
            UUID sourceDocumentUuid,
            UUID targetDocumentUuid
    ) {
        if (sourceDocumentUuid.compareTo(targetDocumentUuid) < 0) {
            return new GraphEdgeResponse(
                    sourceDocumentUuid,
                    targetDocumentUuid,
                    GraphEdgeType.REFERENCE
            );
        }
        return new GraphEdgeResponse(
                targetDocumentUuid,
                sourceDocumentUuid,
                GraphEdgeType.REFERENCE
        );
    }
}
