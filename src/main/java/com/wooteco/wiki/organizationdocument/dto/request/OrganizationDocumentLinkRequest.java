package com.wooteco.wiki.organizationdocument.dto.request;

import java.util.UUID;

public record OrganizationDocumentLinkRequest(
        UUID crewDocumentUuid,
        UUID organizationDocumentUuid
) {
}