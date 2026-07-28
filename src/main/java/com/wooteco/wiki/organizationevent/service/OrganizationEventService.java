package com.wooteco.wiki.organizationevent.service;

import static com.wooteco.wiki.global.exception.ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND;
import static com.wooteco.wiki.global.exception.ErrorCode.ORGANIZATION_EVENT_NOT_FOUND;

import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationevent.domain.OrganizationEvent;
import com.wooteco.wiki.organizationevent.dto.request.OrganizationEventCreateRequest;
import com.wooteco.wiki.organizationevent.dto.request.OrganizationEventUpdateRequest;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventCreateResponse;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventUpdateResponse;
import com.wooteco.wiki.organizationevent.repository.OrganizationEventRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class OrganizationEventService {

    private final OrganizationEventRepository organizationEventRepository;
    private final OrganizationDocumentRepository organizationDocumentRepository;

    public OrganizationEventCreateResponse post(OrganizationEventCreateRequest request) {
        OrganizationDocument organizationDocument = getOrganizationDocumentByUuid(request.organizationDocumentUuid());
        OrganizationEvent event = request.toOrganizationEvent(organizationDocument);
        OrganizationEvent savedEvent = organizationEventRepository.save(event);
        return OrganizationEventCreateResponse.from(savedEvent);
    }

    public OrganizationEventUpdateResponse put(
            UUID organizationEventUuid,
            OrganizationEventUpdateRequest request
    ) {
        OrganizationEvent organizationEvent = getOrganizationEvent(organizationEventUuid);
        organizationEvent.update(
                request.title(),
                request.contents(),
                request.writer(),
                request.occurredAt()
        );
        return OrganizationEventUpdateResponse.from(organizationEvent);
    }

    public void delete(UUID organizationEventUuid) {
        OrganizationEvent organizationEvent = getOrganizationEvent(organizationEventUuid);

        organizationEventRepository.delete(organizationEvent);
    }

    private OrganizationDocument getOrganizationDocumentByUuid(UUID uuid) {
        return organizationDocumentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ORGANIZATION_DOCUMENT_NOT_FOUND));
    }

    private OrganizationEvent getOrganizationEvent(UUID uuid) {
        return organizationEventRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ORGANIZATION_EVENT_NOT_FOUND));
    }
}
