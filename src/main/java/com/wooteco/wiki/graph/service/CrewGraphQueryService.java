package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.CrewGraphElements;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeType;
import com.wooteco.wiki.graph.dto.GraphNodeResponse;
import com.wooteco.wiki.graph.repository.CrewGraphQueryRepository;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CrewGraphQueryService {

    private final CrewGraphReader crewGraphReader;
    private final CrewGraphQueryRepository crewGraphQueryRepository;
    private final OrganizationDocumentRepository organizationDocumentRepository;

    @Transactional(readOnly = true)
    public CrewGraphResponse findByGeneration(String generation) {
        return findByGeneration(generation, null);
    }

    // 크루 node와 edge는 graph.read.source가 고른 reader가 만들고, 조직 그래프 합성만 여기서 담당한다.
    @Transactional(readOnly = true)
    public CrewGraphResponse findByGeneration(
            String generation,
            UUID organizationDocumentUuid
    ) {
        validateGeneration(generation);
        CrewGraphElements crewGraphElements = crewGraphReader.read(generation);
        List<GraphNodeResponse> nodes = new ArrayList<>(crewGraphElements.nodes());
        List<GraphEdgeResponse> edges = new ArrayList<>(crewGraphElements.edges());
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
