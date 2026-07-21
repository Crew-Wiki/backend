package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.domain.CrewField;
import com.wooteco.wiki.document.dto.GenerationCrewResponse;
import com.wooteco.wiki.document.repository.GenerationCrewOrganizationReadModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class GenerationCrewCandidates {

    private final List<GenerationCrewCandidate> values;

    private GenerationCrewCandidates(List<GenerationCrewCandidate> values) {
        this.values = List.copyOf(values);
    }

    static GenerationCrewCandidates from(List<GenerationCrewOrganizationReadModel> readModels) {
        Map<CrewIdentity, List<String>> organizationTitlesByCrew = new LinkedHashMap<>();
        for (GenerationCrewOrganizationReadModel readModel : readModels) {
            addOrganizationTitle(organizationTitlesByCrew, readModel);
        }
        List<GenerationCrewCandidate> candidates = createCandidates(organizationTitlesByCrew);
        return new GenerationCrewCandidates(candidates);
    }

    List<GenerationCrewResponse> extractResponses(CrewProfileExtractor crewProfileExtractor) {
        List<GenerationCrewResponse> responses = new ArrayList<>();
        for (GenerationCrewCandidate candidate : values) {
            Optional<GenerationCrewResponse> response = candidate.extractResponse(crewProfileExtractor);
            response.ifPresent(responses::add);
        }
        return responses;
    }

    private static void addOrganizationTitle(
            Map<CrewIdentity, List<String>> organizationTitlesByCrew,
            GenerationCrewOrganizationReadModel readModel
    ) {
        CrewIdentity crewIdentity = CrewIdentity.from(readModel);
        List<String> organizationTitles = organizationTitlesByCrew.computeIfAbsent(
                crewIdentity,
                ignored -> new ArrayList<>()
        );
        organizationTitles.add(readModel.organizationTitle());
    }

    private static List<GenerationCrewCandidate> createCandidates(
            Map<CrewIdentity, List<String>> organizationTitlesByCrew
    ) {
        List<GenerationCrewCandidate> candidates = new ArrayList<>();
        organizationTitlesByCrew.forEach((crewIdentity, organizationTitles) -> candidates.add(
                GenerationCrewCandidate.of(crewIdentity, organizationTitles)
        ));
        return candidates;
    }

    private record CrewIdentity(
            String title,
            UUID documentUuid
    ) {

        private static CrewIdentity from(GenerationCrewOrganizationReadModel readModel) {
            return new CrewIdentity(readModel.crewTitle(), readModel.documentUuid());
        }
    }

    private record GenerationCrewCandidate(
            CrewIdentity crewIdentity,
            List<String> organizationTitles
    ) {

        private GenerationCrewCandidate {
            organizationTitles = List.copyOf(organizationTitles);
        }

        private static GenerationCrewCandidate of(
                CrewIdentity crewIdentity,
                List<String> organizationTitles
        ) {
            return new GenerationCrewCandidate(crewIdentity, organizationTitles);
        }

        private Optional<GenerationCrewResponse> extractResponse(
                CrewProfileExtractor crewProfileExtractor
        ) {
            Optional<String> name = crewProfileExtractor.extractName(crewIdentity.title());
            if (name.isEmpty()) {
                return Optional.empty();
            }
            CrewField field = crewProfileExtractor.extractField(organizationTitles)
                    .orElse(null);
            GenerationCrewResponse response = GenerationCrewResponse.of(
                    name.get(),
                    crewIdentity.documentUuid(),
                    field
            );
            return Optional.of(response);
        }
    }
}
