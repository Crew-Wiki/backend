package com.wooteco.wiki.document.domain.dto;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentResponse(
    Long documentId,
    UUID documentUUID,
    String title,
    String contents,
    String writer,
    LocalDateTime generateTime,
    Integer viewCount,
    Long latestVersion,
    List<OrganizationDocumentResponse> organizationDocumentResponses
) {

    public static DocumentResponse toDocumentResponse(
        CrewDocument crewDocument,
        Long latestVersion,
        List<OrganizationDocumentResponse> organizationDocumentResponses
    ) {
        return new DocumentResponse(
            crewDocument.getId(),
            crewDocument.getUuid(),
            crewDocument.getTitle(),
            crewDocument.getContents(),
            crewDocument.getWriter(),
            crewDocument.getGenerateTime(),
            crewDocument.getViewCount(),
            latestVersion,
            organizationDocumentResponses
        );
    }
}

