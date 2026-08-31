package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillItemResult;
import com.wooteco.wiki.graph.dto.DocumentReferenceSyncResult;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final DocumentReferenceRepository documentReferenceRepository;

    // 문서 하나의 실패가 다른 문서 처리를 막지 않도록 문서마다 독립 transaction에서 동기화한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DocumentReferenceBackfillItemResult synchronize(UUID sourceDocumentUuid) {
        CrewDocument sourceDocument = crewDocumentRepository.findByUuidForUpdate(sourceDocumentUuid)
                .orElseThrow(() -> new WikiException(ErrorCode.DOCUMENT_NOT_FOUND));
        DocumentReferenceSyncResult syncResult = documentReferenceSyncService.synchronize(sourceDocument);
        boolean mismatched = isMismatched(sourceDocument, syncResult.expectedTargetDocumentUuids());
        return DocumentReferenceBackfillItemResult.of(syncResult, mismatched);
    }

    // 저장 결과를 다시 읽어 현재 본문에서 기대한 target 집합과 같은지 검증한다.
    private boolean isMismatched(
            CrewDocument sourceDocument,
            Set<UUID> expectedTargetDocumentUuids
    ) {
        Set<UUID> savedTargetDocumentUuids = findSavedTargetDocumentUuids(sourceDocument);
        return !savedTargetDocumentUuids.equals(expectedTargetDocumentUuids);
    }

    private Set<UUID> findSavedTargetDocumentUuids(CrewDocument sourceDocument) {
        List<DocumentReference> savedReferences = documentReferenceRepository.findAllBySourceDocument(sourceDocument);
        Set<UUID> savedTargetDocumentUuids = new HashSet<>();
        for (DocumentReference savedReference : savedReferences) {
            addTargetDocumentUuid(savedTargetDocumentUuids, savedReference);
        }
        return savedTargetDocumentUuids;
    }

    private void addTargetDocumentUuid(
            Set<UUID> savedTargetDocumentUuids,
            DocumentReference savedReference
    ) {
        UUID targetDocumentUuid = savedReference.getTargetDocument()
                .getUuid();
        savedTargetDocumentUuids.add(targetDocumentUuid);
    }
}
