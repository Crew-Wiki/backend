package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.dto.DocumentUuidResponse;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.common.PagingRequest;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public Page<Document> findAll(PagingRequest pagingRequest) {
        return documentRepository.findAll(pagingRequest.toPageable());
    }

    public DocumentUuidResponse getUuidByTitle(String title) {
        return documentRepository.findUuidByTitle(title)
            .map(DocumentUuidResponse::new)
            .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Transactional
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
}

