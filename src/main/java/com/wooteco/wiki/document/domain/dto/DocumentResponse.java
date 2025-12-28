package com.wooteco.wiki.document.domain.dto;

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
}

