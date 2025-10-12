package com.wooteco.wiki.organizationdocument.dto.request;

import java.util.UUID;

public record OrganizationDocumentCreateRequest(
        String title,
        String contents,
        String writer,
        Long documentBytes,
        UUID uuid
) {
}
