package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.graph.dto.CrewGraphElements;
import com.wooteco.wiki.graph.dto.GraphNodeResponse;
import com.wooteco.wiki.graph.repository.CrewGraphNodeReadModel;
import com.wooteco.wiki.graph.repository.CrewGraphQueryRepository;
import com.wooteco.wiki.graph.repository.DocumentReferenceReadModel;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// 본문을 읽어 참조를 파싱하지 않고 저장된 node와 reference 조회 결과로 그래프를 조립한다.
// document_reference backfill이 끝난 뒤에만 정확한 결과를 낸다.
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "graph.read.source",
        havingValue = "persisted"
)
@Component
public class PersistedCrewGraphReader implements CrewGraphReader {

    private final CrewGraphQueryRepository crewGraphQueryRepository;
    private final DocumentReferenceRepository documentReferenceRepository;
    private final CrewGraphEdgeNormalizer crewGraphEdgeNormalizer;

    @Override
    public CrewGraphElements read(String generation) {
        List<CrewGraphNodeReadModel> nodeReadModels = crewGraphQueryRepository
                .findAllGraphNodesByGenerationTitle(generation);
        List<DocumentReferenceReadModel> referenceReadModels = documentReferenceRepository
                .findAllReadModelsByGenerationTitle(generation);
        return CrewGraphElements.of(
                createCrewNodes(nodeReadModels),
                crewGraphEdgeNormalizer.normalize(referenceReadModels)
        );
    }

    private List<GraphNodeResponse> createCrewNodes(List<CrewGraphNodeReadModel> nodeReadModels) {
        List<GraphNodeResponse> nodes = new ArrayList<>();
        for (CrewGraphNodeReadModel nodeReadModel : nodeReadModels) {
            nodes.add(GraphNodeResponse.fromCrewDocument(
                    nodeReadModel.documentUuid(),
                    nodeReadModel.title()
            ));
        }
        return List.copyOf(nodes);
    }
}
