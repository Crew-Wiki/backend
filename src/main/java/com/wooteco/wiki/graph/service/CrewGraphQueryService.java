package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeResponse;
import com.wooteco.wiki.graph.dto.GraphNodeResponse;
import com.wooteco.wiki.graph.repository.CrewGraphQueryRepository;
import com.wooteco.wiki.graph.repository.CrewGraphReadModel;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CrewGraphQueryService {

    private final CrewGraphQueryRepository crewGraphQueryRepository;

    @Transactional(readOnly = true)
    public CrewGraphResponse findByGeneration(String generation) {
        validateGeneration(generation);
        List<CrewGraphReadModel> readModels = crewGraphQueryRepository.findAllCrewDocumentsByGenerationTitle(generation);
        List<GraphNodeResponse> nodes = createNodes(readModels);
        List<GraphEdgeResponse> edges = List.of();
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
}
