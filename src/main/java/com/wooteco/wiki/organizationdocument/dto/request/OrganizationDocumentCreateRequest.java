package com.wooteco.wiki.organizationdocument.dto.request;

import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import java.util.UUID;

public record OrganizationDocumentCreateRequest(
        String title,
        String contents,
        String writer,
        Long documentBytes,
        UUID uuid
) {
    public OrganizationDocument toOrganizationDocument() {
        return new OrganizationDocument(title, contents, writer, documentBytes, uuid);
    }
}
