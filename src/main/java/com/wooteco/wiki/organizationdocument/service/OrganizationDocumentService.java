package com.wooteco.wiki.organizationdocument.service;

import static com.wooteco.wiki.global.exception.ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND;

import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentCreateRequest;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentUpdateRequest;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentAndEventResponse;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventResponse;
import com.wooteco.wiki.organizationevent.repository.OrganizationEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class OrganizationDocumentService {

    private final OrganizationDocumentRepository organizationDocumentRepository;
    private final OrganizationEventRepository organizationEventRepository;
    private final DocumentRepository documentRepository;

    public OrganizationDocumentResponse create(OrganizationDocumentCreateRequest organizationDocumentCreateRequest) {
        if (documentRepository.existsByTitle(organizationDocumentCreateRequest.title())) {
            throw new WikiException(ErrorCode.DOCUMENT_DUPLICATE);
        }
        OrganizationDocument organizationDocument = organizationDocumentCreateRequest.toOrganizationDocument();
        organizationDocumentRepository.save(organizationDocument);
        return new OrganizationDocumentResponse(organizationDocument);
    }

    public OrganizationDocumentResponse update(OrganizationDocumentUpdateRequest organizationDocumentUpdateRequest) {
        OrganizationDocument organizationDocument = organizationDocumentRepository.findByUuid(
                        organizationDocumentUpdateRequest.uuid())
                .orElseThrow(() -> new WikiException(ORGANIZATION_DOCUMENT_NOT_FOUND));

        organizationDocument.update(
                organizationDocumentUpdateRequest.title(),
                organizationDocumentUpdateRequest.contents(),
                organizationDocumentUpdateRequest.writer(),
                organizationDocumentUpdateRequest.documentBytes(),
                LocalDateTime.now()
        );
        return new OrganizationDocumentResponse(organizationDocument);
    }

    @Transactional(readOnly = true)
    public OrganizationDocumentAndEventResponse findByUuid(UUID uuidText) {
        OrganizationDocument organizationDocument = getOrganizationDocument(uuidText);
        List<OrganizationEventResponse> organizationEventResponses = organizationEventRepository.findAllByOrganizationDocumentUuid(
                        uuidText)
                .stream()
                .map(OrganizationEventResponse::new)
                .toList();
        return new OrganizationDocumentAndEventResponse(organizationDocument, organizationEventResponses);
    }

    private OrganizationDocument getOrganizationDocument(UUID uuid) {
        return organizationDocumentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ORGANIZATION_DOCUMENT_NOT_FOUND));
    }
}
