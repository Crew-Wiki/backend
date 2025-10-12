package com.wooteco.wiki.organizationevent.dto.response;

import com.wooteco.wiki.organizationevent.domain.OrganizationEvent;
import java.time.LocalDate;
import java.util.UUID;

public record OrganizationEventResponse(
        UUID organizationEventUuid,
        String title,
        String contents,
        String writer,
        LocalDate occurredAt
) {
    public OrganizationEventResponse(OrganizationEvent organizationEvent) {
        this(organizationEvent.getUuid(), organizationEvent.getTitle(), organizationEvent.getContents(), organizationEvent.getWriter(), organizationEvent.getOccurredAt());
    }
}
