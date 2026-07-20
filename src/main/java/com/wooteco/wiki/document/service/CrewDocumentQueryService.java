package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.dto.GenerationCrewResponse;
import com.wooteco.wiki.document.repository.GenerationCrewOrganizationReadModel;
import com.wooteco.wiki.document.repository.GenerationCrewQueryRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CrewDocumentQueryService {

    private static final String GENERATION_SUFFIX = "기";

    private final GenerationCrewQueryRepository generationCrewQueryRepository;
    private final CrewProfileExtractor crewProfileExtractor;

    @Transactional(readOnly = true)
    public List<GenerationCrewResponse> findAllByGeneration(Integer generation) {
        validateGeneration(generation);
        String generationTitle = generation + GENERATION_SUFFIX;
        List<GenerationCrewOrganizationReadModel> readModels = generationCrewQueryRepository
                .findAllByGenerationTitle(generationTitle);
        GenerationCrewCandidates candidates = GenerationCrewCandidates.from(readModels);
        List<GenerationCrewResponse> responses = candidates.extractResponses(crewProfileExtractor);
        responses.sort(Comparator.comparing(GenerationCrewResponse::name));
        return List.copyOf(responses);
    }

    private void validateGeneration(Integer generation) {
        if (generation == null || generation <= 0) {
            throw new WikiException(ErrorCode.VALIDATION_ERROR);
        }
    }

}
