package com.wooteco.wiki.organizationdocument.dto.response;

import com.wooteco.wiki.document.domain.CrewDocument;
import java.util.UUID;

public record LinkedCrewDocumentResponse(
        UUID documentUuid,
        String title
) {

    public LinkedCrewDocumentResponse(CrewDocument crewDocument) {
        this(
                crewDocument.getUuid(),
                crewDocument.getTitle()
        );
    }
}