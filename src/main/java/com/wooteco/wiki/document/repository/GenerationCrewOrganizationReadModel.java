package com.wooteco.wiki.document.repository;

import java.util.UUID;

public record GenerationCrewOrganizationReadModel(
        String crewTitle,
        UUID documentUuid,
        String organizationTitle
) {
}
