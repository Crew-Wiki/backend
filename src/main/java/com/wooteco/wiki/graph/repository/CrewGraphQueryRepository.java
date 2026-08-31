package com.wooteco.wiki.graph.repository;

import java.util.List;
import java.util.UUID;

public interface CrewGraphQueryRepository {

    List<CrewGraphNodeReadModel> findAllGraphNodesByGenerationTitle(String generationTitle);

    List<CrewGraphReadModel> findAllCrewDocumentsByGenerationTitle(String generationTitle);

    List<UUID> findAllCrewDocumentUuidsByGenerationTitleAndOrganizationDocumentUuid(
            String generationTitle,
            UUID organizationDocumentUuid
    );
}
