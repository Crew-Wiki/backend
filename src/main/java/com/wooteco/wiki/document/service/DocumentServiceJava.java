package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.dto.DocumentOrganizationMappingAddRequest;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.OrganizationDocumentSearchResponse;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationdocument.service.DocumentOrganizationLinkService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DocumentServiceJava {

    private final DocumentOrganizationLinkService documentOrganizationLinkService;
    private final OrganizationDocumentRepository organizationDocumentRepository;
    private final CrewDocumentRepository crewDocumentRepository;

    public List<OrganizationDocumentSearchResponse> searchOrganizationDocument(UUID documentUuid) {
        CrewDocument crewDocument = getCrewDocument(documentUuid);
        List<OrganizationDocumentResponse> organizationDocumentResponsesByDocument = documentOrganizationLinkService.findOrganizationDocumentResponsesByDocument(
                crewDocument);

        return toOrganizationDocumentTitleAndUuidResponses(organizationDocumentResponsesByDocument);
    }

    public void addOrganizationDocument(
            UUID documentUuid,
            DocumentOrganizationMappingAddRequest documentOrganizationMappingAddRequest
    ) {
        CrewDocument crewDocument = getCrewDocument(documentUuid);
        OrganizationDocument organizationDocument = organizationDocumentRepository.save(
                documentOrganizationMappingAddRequest.toOrganizationDocument());

        documentOrganizationLinkService.link(crewDocument, organizationDocument);
    }

    public void deleteOrganizationDocument(
            UUID documentUuid,
            UUID organizationDocumentUuid
    ) {
        CrewDocument crewDocument = getCrewDocument(documentUuid);
        OrganizationDocument organizationDocument = getOrganizationDocument(organizationDocumentUuid);

        documentOrganizationLinkService.unlink(crewDocument, organizationDocument);
    }

    private List<OrganizationDocumentSearchResponse> toOrganizationDocumentTitleAndUuidResponses(
            List<OrganizationDocumentResponse> organizationDocumentResponsesByDocument) {
        return organizationDocumentResponsesByDocument.stream()
                .map(OrganizationDocumentResponse::toOrganizationDocumentTitleAndUuidResponse)
                .toList();
    }

    private CrewDocument getCrewDocument(UUID uuid) {
        return crewDocumentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private OrganizationDocument getOrganizationDocument(UUID uuid) {
        return organizationDocumentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND));
    }
}
