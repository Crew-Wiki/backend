package com.wooteco.wiki.admin.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.history.service.HistoryService;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.service.DocumentOrganizationLinkService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CrewDocumentService {

    private final DocumentOrganizationLinkService documentOrganizationLinkService;
    private final CrewDocumentRepository crewDocumentRepository;
    private final DocumentRepository documentRepository;
    private final HistoryService historyService;
    private final Random random;

    @Transactional
    public void deleteByUuid(UUID documentUuid) {
        CrewDocument crewDocument = crewDocumentRepository.findByUuid(documentUuid)
            .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));

        documentOrganizationLinkService.unlinkAll(crewDocument);

        documentRepository.deleteByUuid(documentUuid);
    }

    @Transactional
    public DocumentResponse create(CrewDocumentCreateRequest request) {
        String title = request.title();
        if (documentRepository.existsByTitle(title)) {
            throw new WikiException(ErrorCode.DOCUMENT_DUPLICATE);
        }

        CrewDocument crewDocument = request.toCrewDocument();
        CrewDocument savedDocument = crewDocumentRepository.save(crewDocument);
        historyService.save(savedDocument);
        return mapToResponse(savedDocument);
    }

    public DocumentResponse getByUuid(UUID uuid) {
        CrewDocument crewDocument = crewDocumentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
        return mapToResponse(crewDocument);
    }

    public DocumentResponse getByTitle(String title) {
        Document document = documentRepository.findByTitle(title)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));

        if (!(document instanceof CrewDocument crewDocument)) {
            throw new WikiException(ErrorCode.DOCUMENT_NOT_FOUND);
        }

        return mapToResponse(crewDocument);
    }

    public DocumentResponse getRandom() {
        List<CrewDocument> documents = crewDocumentRepository.findAll();
        if (documents.isEmpty()) {
            throw new WikiException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        CrewDocument document = documents.get(random.nextInt(documents.size()));
        return mapToResponse(document);
    }

    @Transactional
    public DocumentResponse update(UUID uuid, DocumentUpdateRequest request) {
        CrewDocument crewDocument = crewDocumentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));

        Document updateData = crewDocument.update(
                request.title(),
                request.contents(),
                request.writer(),
                request.documentBytes(),
                LocalDateTime.now()
        );
        historyService.save(updateData);
        return mapToResponse(crewDocument);
    }

    private DocumentResponse mapToResponse(CrewDocument crewDocument) {
        long latestVersion = historyService.findLatestVersionByDocument(crewDocument);
        List<OrganizationDocumentResponse> organizationDocumentResponses =
                documentOrganizationLinkService.findOrganizationDocumentResponsesByDocument(crewDocument);

        return DocumentResponse.toDocumentResponse(crewDocument, latestVersion, organizationDocumentResponses);
    }
}
