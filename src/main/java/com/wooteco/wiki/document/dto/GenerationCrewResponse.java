package com.wooteco.wiki.document.dto;

import com.wooteco.wiki.document.domain.CrewField;
import java.util.UUID;

public record GenerationCrewResponse(
        String name,
        UUID documentUuid,
        CrewField field
) {

    public static GenerationCrewResponse of(
            String name,
            UUID documentUuid,
            CrewField field
    ) {
        return new GenerationCrewResponse(name, documentUuid, field);
    }
}
