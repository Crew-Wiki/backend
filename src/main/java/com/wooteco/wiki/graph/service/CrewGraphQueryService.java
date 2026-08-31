package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeType;
import com.wooteco.wiki.graph.dto.GraphNodeResponse;
import com.wooteco.wiki.graph.repository.CrewGraphNodeReadModel;
import com.wooteco.wiki.graph.repository.CrewGraphQueryRepository;
import com.wooteco.wiki.graph.repository.DocumentReferenceReadModel;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CrewGraphQueryService {

    private final CrewGraphQueryRepository crewGraphQueryRepository;
    private final DocumentReferenceRepository documentReferenceRepository;
    private final OrganizationDocumentRepository organizationDocumentRepository;

    @Transactional(readOnly = true)
    public CrewGraphResponse findByGeneration(String generation) {
        return findByGeneration(generation, null);
    }

    // 본문을 읽어 참조를 파싱하지 않고 저장된 node와 reference 조회 결과로 응답을 조립한다.
    @Transactional(readOnly = true)
    public CrewGraphResponse findByGeneration(
            String generation,
            UUID organizationDocumentUuid
    ) {
        validateGeneration(generation);
        List<CrewGraphNodeReadModel> nodeReadModels = crewGraphQueryRepository
                .findAllGraphNodesByGenerationTitle(generation);
        List<DocumentReferenceReadModel> referenceReadModels = documentReferenceRepository
                .findAllReadModelsByGenerationTitle(generation);
        List<GraphNodeResponse> nodes = new ArrayList<>(createCrewNodes(nodeReadModels));
        List<GraphEdgeResponse> edges = new ArrayList<>(createReferenceEdges(referenceReadModels));
        addOrganizationGraphIfSelected(
                generation,
                organizationDocumentUuid,
                nodes,
                edges
        );
        return CrewGraphResponse.of(nodes, edges);
    }

    private void validateGeneration(String generation) {
        if (generation == null || generation.isBlank()) {
            throw new WikiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private List<GraphNodeResponse> createCrewNodes(List<CrewGraphNodeReadModel> nodeReadModels) {
        List<GraphNodeResponse> nodes = new ArrayList<>();
        for (CrewGraphNodeReadModel nodeReadModel : nodeReadModels) {
            GraphNodeResponse node = GraphNodeResponse.fromCrewDocument(
                    nodeReadModel.documentUuid(),
                    nodeReadModel.title()
            );
            nodes.add(node);
        }
        return List.copyOf(nodes);
    }

    // DB는 방향성 참조를 저장하지만 API는 UUID가 작은 문서를 source로 둔 무방향 edge 하나만 반환한다.
    private List<GraphEdgeResponse> createReferenceEdges(List<DocumentReferenceReadModel> referenceReadModels) {
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

    private void addOrganizationGraphIfSelected(
            String generation,
            UUID organizationDocumentUuid,
            List<GraphNodeResponse> nodes,
            List<GraphEdgeResponse> edges
    ) {
        if (organizationDocumentUuid == null) {
            return;
        }
        OrganizationDocument organizationDocument = findOrganizationDocument(organizationDocumentUuid);
        validateOrganizationIsNotGeneration(generation, organizationDocument);
        if (nodes.isEmpty()) {
            return;
        }
        nodes.add(GraphNodeResponse.fromOrganizationDocument(
                organizationDocument.getUuid(),
                organizationDocument.getTitle()
        ));
        List<UUID> linkedCrewDocumentUuids = crewGraphQueryRepository
                .findAllCrewDocumentUuidsByGenerationTitleAndOrganizationDocumentUuid(
                        generation,
                        organizationDocumentUuid
                );
        addOrganizationLinkEdges(
                organizationDocumentUuid,
                linkedCrewDocumentUuids,
                edges
        );
    }

    private OrganizationDocument findOrganizationDocument(UUID organizationDocumentUuid) {
        Optional<OrganizationDocument> organizationDocument = organizationDocumentRepository.findByUuid(
                organizationDocumentUuid
        );
        return organizationDocument.orElseThrow(
                () -> new WikiException(ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND)
        );
    }

    private void validateOrganizationIsNotGeneration(
            String generation,
            OrganizationDocument organizationDocument
    ) {
        if (generation.equals(organizationDocument.getTitle())) {
            throw new WikiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void addOrganizationLinkEdges(
            UUID organizationDocumentUuid,
            List<UUID> linkedCrewDocumentUuids,
            List<GraphEdgeResponse> edges
    ) {
        for (UUID crewDocumentUuid : linkedCrewDocumentUuids) {
            edges.add(new GraphEdgeResponse(
                    organizationDocumentUuid,
                    crewDocumentUuid,
                    GraphEdgeType.ORGANIZATION_LINK
            ));
        }
    }
}
