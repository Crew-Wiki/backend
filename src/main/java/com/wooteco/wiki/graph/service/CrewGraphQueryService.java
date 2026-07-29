package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeType;
import com.wooteco.wiki.graph.dto.GraphNodeResponse;
import com.wooteco.wiki.graph.repository.CrewGraphQueryRepository;
import com.wooteco.wiki.graph.repository.CrewGraphReadModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CrewGraphQueryService {

    private final CrewGraphQueryRepository crewGraphQueryRepository;
    private final CrewDocumentReferenceExtractor crewDocumentReferenceExtractor;

    @Transactional(readOnly = true)
    public CrewGraphResponse findByGeneration(String generation) {
        validateGeneration(generation);
        List<CrewGraphReadModel> readModels = crewGraphQueryRepository.findAllCrewDocumentsByGenerationTitle(generation);
        List<GraphNodeResponse> nodes = createNodes(readModels);
        List<GraphEdgeResponse> edges = createEdges(readModels);
        return CrewGraphResponse.of(nodes, edges);
    }

    private void validateGeneration(String generation) {
        if (generation == null || generation.isBlank()) {
            throw new WikiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private List<GraphNodeResponse> createNodes(List<CrewGraphReadModel> readModels) {
        List<GraphNodeResponse> nodes = new ArrayList<>();
        for (CrewGraphReadModel readModel : readModels) {
            GraphNodeResponse node = GraphNodeResponse.fromCrewDocument(
                    readModel.documentUuid(),
                    readModel.title()
            );
            nodes.add(node);
        }
        return List.copyOf(nodes);
    }

    private List<GraphEdgeResponse> createEdges(List<CrewGraphReadModel> readModels) {
        Set<UUID> nodeDocumentUuids = createNodeDocumentUuids(readModels);
        Set<GraphEdgeResponse> edges = new HashSet<>();
        for (CrewGraphReadModel readModel : readModels) {
            addReferenceEdges(readModel, nodeDocumentUuids, edges);
        }
        List<GraphEdgeResponse> sortedEdges = new ArrayList<>(edges);
        sortedEdges.sort(Comparator
                .comparing(GraphEdgeResponse::sourceDocumentUuid)
                .thenComparing(GraphEdgeResponse::targetDocumentUuid));
        return List.copyOf(sortedEdges);
    }

    private Set<UUID> createNodeDocumentUuids(List<CrewGraphReadModel> readModels) {
        Set<UUID> documentUuids = new HashSet<>();
        for (CrewGraphReadModel readModel : readModels) {
            documentUuids.add(readModel.documentUuid());
        }
        return documentUuids;
    }

    private void addReferenceEdges(
            CrewGraphReadModel sourceDocument,
            Set<UUID> nodeDocumentUuids,
            Set<GraphEdgeResponse> edges
    ) {
        List<UUID> referencedDocumentUuids = crewDocumentReferenceExtractor.extract(sourceDocument.contents());
        for (UUID targetDocumentUuid : referencedDocumentUuids) {
            addReferenceEdgeIfValid(
                    sourceDocument.documentUuid(),
                    targetDocumentUuid,
                    nodeDocumentUuids,
                    edges
            );
        }
    }

    private void addReferenceEdgeIfValid(
            UUID sourceDocumentUuid,
            UUID targetDocumentUuid,
            Set<UUID> nodeDocumentUuids,
            Set<GraphEdgeResponse> edges
    ) {
        if (sourceDocumentUuid.equals(targetDocumentUuid)) {
            return;
        }
        if (!nodeDocumentUuids.contains(targetDocumentUuid)) {
            return;
        }
        GraphEdgeResponse edge = createReferenceEdge(sourceDocumentUuid, targetDocumentUuid);
        edges.add(edge);
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
