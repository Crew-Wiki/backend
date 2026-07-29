package com.wooteco.wiki.graph.repository;

import java.util.List;

public interface CrewGraphQueryRepository {

    List<CrewGraphReadModel> findAllCrewDocumentsByGenerationTitle(String generationTitle);
}
