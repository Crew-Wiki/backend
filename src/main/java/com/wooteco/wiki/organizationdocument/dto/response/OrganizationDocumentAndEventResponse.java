package com.wooteco.wiki.organizationdocument.dto.response;

import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationevent.domain.OrganizationEvent;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrganizationDocumentAndEventResponse(
        Long organizationDocumentId,
        UUID organizationDocumentUuid,
        String title,
        String contents,
        String writer,
        LocalDateTime generateTime,
        List<OrganizationEventResponse> organizationEventResponses
) {

    public OrganizationDocumentAndEventResponse(
            OrganizationDocument organizationDocument,
            List<OrganizationEventResponse> organizationEventResponses
    ) {
        this(organizationDocument.getId(), organizationDocument.getUuid(), organizationDocument.getTitle(), organizationDocument.getContents(), organizationDocument.getWriter(), organizationDocument.getGenerateTime(), organizationEventResponses);
    }
}
