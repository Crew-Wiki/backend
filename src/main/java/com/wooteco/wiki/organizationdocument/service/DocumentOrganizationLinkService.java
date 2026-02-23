package com.wooteco.wiki.organizationdocument.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class DocumentOrganizationLinkService {

    private final DocumentOrganizationLinkRepository documentOrgDocLinkRepository;

    public void link(CrewDocument crewDocument, OrganizationDocument organizationDocument) {
        DocumentOrganizationLink documentOrgDocLink = new DocumentOrganizationLink(crewDocument,
                organizationDocument);
        documentOrgDocLinkRepository.save(documentOrgDocLink);
    }

    public void unlink(CrewDocument crewDocument, OrganizationDocument organizationDocument) {
        documentOrgDocLinkRepository.deleteByCrewDocumentAndOrganizationDocument(crewDocument, organizationDocument);
    }

    public void unlinkAll(CrewDocument crewDocument) {
        documentOrgDocLinkRepository.deleteAllByCrewDocument(crewDocument);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDocumentResponse> findOrganizationDocumentResponsesByDocument(CrewDocument crewDocument) {
        List<DocumentOrganizationLink> documentOrganizationLinks = documentOrgDocLinkRepository.findAllByCrewDocument(
                crewDocument);
        List<OrganizationDocument> organizationDocuments = documentOrganizationLinks.stream()
                .map(DocumentOrganizationLink::getOrganizationDocument)
                .toList();

        return organizationDocuments.stream()
                .map(OrganizationDocumentResponse::new)
                .toList();
    }
}
