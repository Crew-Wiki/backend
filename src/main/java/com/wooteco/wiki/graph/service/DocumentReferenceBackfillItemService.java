package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillItemResult;
import com.wooteco.wiki.graph.dto.DocumentReferenceSyncResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DocumentReferenceBackfillItemService {

    private final CrewDocumentRepository crewDocumentRepository;
    private final DocumentReferenceSyncService documentReferenceSyncService;

    // 문서 하나의 실패가 다른 문서 처리를 막지 않도록 문서마다 독립 transaction에서 동기화한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DocumentReferenceBackfillItemResult synchronize(UUID sourceDocumentUuid) {
        CrewDocument sourceDocument = crewDocumentRepository.findByUuidForUpdate(sourceDocumentUuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
        DocumentReferenceSyncResult syncResult = documentReferenceSyncService.synchronize(sourceDocument);
        return DocumentReferenceBackfillItemResult.of(syncResult);
    }
}
