package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.DocumentType;
import com.wooteco.wiki.document.domain.dto.DocumentSearchResponse;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentSearchService {

    private final DocumentRepository documentRepository;

    public DocumentSearchService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentSearchResponse> search(String keyWord) {
        return documentRepository.findAllByTitleStartingWith(keyWord)
                .stream()
                .map(document -> new DocumentSearchResponse(
                        document.getTitle(),
                        document.getUuid(),
                        toType(document)
                ))
                .toList();
    }

    private DocumentType toType(Document document) {
        if (document instanceof CrewDocument) {
            return DocumentType.CREW;
        }
        if (document instanceof OrganizationDocument) {
            return DocumentType.ORGANIZATION;
        }
        throw new WikiException(ErrorCode.UNKNOWN_ERROR);
    }
}

