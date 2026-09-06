package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.graph.dto.CrewGraphElements;
import com.wooteco.wiki.graph.dto.GraphNodeResponse;
import com.wooteco.wiki.graph.repository.CrewGraphQueryRepository;
import com.wooteco.wiki.graph.repository.CrewGraphReadModel;
import com.wooteco.wiki.graph.repository.DocumentReferenceReadModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// backfill 이전 데이터에서도 동작하도록 요청 시 본문을 파싱해 참조를 추출하는 기존 읽기 경로다.
// 영속 참조로 완전히 전환해 안정화 판정이 끝나면 제거한다.
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "graph.read.source",
        havingValue = "legacy",
        matchIfMissing = true
)
@Component
public class ContentsParsingCrewGraphReader implements CrewGraphReader {

    private final CrewGraphQueryRepository crewGraphQueryRepository;
    private final CrewDocumentReferenceExtractor crewDocumentReferenceExtractor;
    private final CrewGraphEdgeNormalizer crewGraphEdgeNormalizer;

    @Override
    public CrewGraphElements read(String generation) {
        List<CrewGraphReadModel> readModels = crewGraphQueryRepository
                .findAllCrewDocumentsByGenerationTitle(generation);
        return CrewGraphElements.of(
                createCrewNodes(readModels),
                crewGraphEdgeNormalizer.normalize(createReferenceReadModels(readModels))
        );
    }

    private List<GraphNodeResponse> createCrewNodes(List<CrewGraphReadModel> readModels) {
        List<GraphNodeResponse> nodes = new ArrayList<>();
        for (CrewGraphReadModel readModel : readModels) {
            nodes.add(GraphNodeResponse.fromCrewDocument(
                    readModel.documentUuid(),
                    readModel.title()
            ));
        }
        return List.copyOf(nodes);
    }

    private List<DocumentReferenceReadModel> createReferenceReadModels(List<CrewGraphReadModel> readModels) {
        Set<UUID> nodeDocumentUuids = createCrewDocumentUuids(readModels);
        List<DocumentReferenceReadModel> referenceReadModels = new ArrayList<>();
        for (CrewGraphReadModel readModel : readModels) {
            addReferenceReadModels(readModel, nodeDocumentUuids, referenceReadModels);
        }
        return List.copyOf(referenceReadModels);
    }

    private Set<UUID> createCrewDocumentUuids(List<CrewGraphReadModel> readModels) {
        Set<UUID> documentUuids = new HashSet<>();
        for (CrewGraphReadModel readModel : readModels) {
            documentUuids.add(readModel.documentUuid());
        }
        return documentUuids;
    }

    private void addReferenceReadModels(
            CrewGraphReadModel sourceDocument,
            Set<UUID> nodeDocumentUuids,
            List<DocumentReferenceReadModel> referenceReadModels
    ) {
        List<UUID> referencedDocumentUuids = crewDocumentReferenceExtractor.extract(sourceDocument.contents());
        for (UUID targetDocumentUuid : referencedDocumentUuids) {
            addReferenceReadModelIfNode(
                    sourceDocument.documentUuid(),
                    targetDocumentUuid,
                    nodeDocumentUuids,
                    referenceReadModels
            );
        }
    }

    // 영속 조회는 같은 기수 크루로 target을 제한하므로, 본문 파싱 경로도 노드 집합으로 동일하게 제한한다.
    private void addReferenceReadModelIfNode(
            UUID sourceDocumentUuid,
            UUID targetDocumentUuid,
            Set<UUID> nodeDocumentUuids,
            List<DocumentReferenceReadModel> referenceReadModels
    ) {
        if (!nodeDocumentUuids.contains(targetDocumentUuid)) {
            return;
        }
        referenceReadModels.add(new DocumentReferenceReadModel(sourceDocumentUuid, targetDocumentUuid));
    }
}
