package com.wooteco.wiki.document.repository;

import java.util.List;

public interface GenerationCrewQueryRepository {

    List<GenerationCrewOrganizationReadModel> findAllByGenerationTitle(String generationTitle);
}
