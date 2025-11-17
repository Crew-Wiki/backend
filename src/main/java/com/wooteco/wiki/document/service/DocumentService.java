package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentUuidResponse;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.common.PageRequestDto;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.history.service.HistoryService;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.service.DocumentOrganizationLinkService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kotlin.random.Random;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentOrganizationLinkService organizationDocumentLinkService;
    private final HistoryService historyService;
    private final Random random;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentOrganizationLinkService organizationDocumentLinkService,
            HistoryService historyService,
            Random random
    ) {
        this.documentRepository = documentRepository;
        this.organizationDocumentLinkService = organizationDocumentLinkService;
        this.historyService = historyService;
        this.random = random;
    }

    public DocumentResponse postCrewDocument(CrewDocumentCreateRequest request) {
        String title = request.title();
        if (documentRepository.existsByTitle(title)) {
            throw new WikiException(ErrorCode.DOCUMENT_DUPLICATE);
        }

        CrewDocument crewDocument = request.toCrewDocument();
        Document savedDocument = documentRepository.save(crewDocument);
        historyService.save(savedDocument);
        return mapToResponse(savedDocument);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getRandom() {
        List<Document> documents = documentRepository.findAll();
        if (documents.isEmpty()) {
            throw new WikiException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        Document document = documents.get(random.nextInt(documents.size()));
        return mapToResponse(document);
    }

    @Transactional(readOnly = true)
    public Page<Document> findAll(PageRequestDto requestDto) {
        return documentRepository.findAll(requestDto.toPageable());
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(String title) {
        Document document = documentRepository.findByTitle(title)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
        return mapToResponse(document);
    }

    @Transactional(readOnly = true)
    public DocumentUuidResponse getUuidByTitle(String title) {
        return documentRepository.findUuidByTitle(title)
                .map(DocumentUuidResponse::new)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public DocumentResponse getByUuid(UUID uuid) {
        Document document = documentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
        return mapToResponse(document);
    }

    public DocumentResponse put(UUID uuid, DocumentUpdateRequest request) {
        String title = request.getTitle();
        String contents = request.getContents();
        String writer = request.getWriter();
        Long documentBytes = request.getDocumentBytes();

        Document document = documentRepository.findByUuid(uuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));

        Document updateData = document.update(title, contents, writer, documentBytes, LocalDateTime.now());
        historyService.save(updateData);
        return mapToResponse(document);
    }

    public void flushViews(Map<UUID, Integer> views) {
        List<Document> documents = documentRepository.findAllByUuidIn(views.keySet());

        for (Document document : documents) {
            Integer countToAdd = views.get(document.getUuid());
            if (countToAdd == null) {
                continue;
            }
            document.changeViewCount(document.getViewCount() + countToAdd);
        }

        documentRepository.saveAll(documents);
    }

    public void deleteByUuid(UUID documentUuid) {
        documentRepository.findByUuid(documentUuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
        documentRepository.deleteByUuid(documentUuid);
    }

    private DocumentResponse mapToResponse(Document document) {
        long latestVersion = historyService.findLatestVersionByDocument(document);
        List<OrganizationDocumentResponse> organizationDocumentResponses =
                (document instanceof CrewDocument crew)
                        ? organizationDocumentLinkService.findOrganizationDocumentResponsesByDocument(crew)
                        : Collections.emptyList();

        return new DocumentResponse(
                document.getId() != null ? document.getId() :
                        throwNotFound(),
                document.getUuid(),
                document.getTitle(),
                document.getContents(),
                document.getWriter(),
                document.getGenerateTime(),
                document.getViewCount(),
                latestVersion,
                organizationDocumentResponses
        );
    }

    private Long throwNotFound() {
        throw new WikiException(ErrorCode.DOCUMENT_NOT_FOUND);
    }
}

