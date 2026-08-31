package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.document.repository.CrewDocumentIdentifierReadModel;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentReferenceBackfillService {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final long FIRST_DOCUMENT_ID_CURSOR = 0L;

    private final CrewDocumentRepository crewDocumentRepository;
    private final DocumentReferenceBackfillItemService documentReferenceBackfillItemService;

    public DocumentReferenceBackfillResult backfill() {
        return backfill(DEFAULT_PAGE_SIZE);
    }

    // 문서별 transaction proxy가 적용되도록 orchestration은 transaction 없이 item service를 호출한다.
    public DocumentReferenceBackfillResult backfill(int pageSize) {
        validatePageSize(pageSize);
        DocumentReferenceBackfillAccumulator accumulator = new DocumentReferenceBackfillAccumulator();
        long lastDocumentId = FIRST_DOCUMENT_ID_CURSOR;
        List<CrewDocumentIdentifierReadModel> identifiers = findNextIdentifiers(lastDocumentId, pageSize);
        while (!identifiers.isEmpty()) {
            accumulatePage(accumulator, identifiers);
            lastDocumentId = findLastDocumentId(identifiers);
            identifiers = findNextIdentifiers(lastDocumentId, pageSize);
        }
        return accumulator.toResult();
    }

    private void validatePageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new WikiException(ErrorCode.PAGE_BAD_REQUEST);
        }
    }

    private List<CrewDocumentIdentifierReadModel> findNextIdentifiers(
            long lastDocumentId,
            int pageSize
    ) {
        return crewDocumentRepository.findIdentifiersAfterId(lastDocumentId, PageRequest.ofSize(pageSize));
    }

    private void accumulatePage(
            DocumentReferenceBackfillAccumulator accumulator,
            List<CrewDocumentIdentifierReadModel> identifiers
    ) {
        for (CrewDocumentIdentifierReadModel identifier : identifiers) {
            accumulateOne(accumulator, identifier.uuid());
        }
    }

    private void accumulateOne(
            DocumentReferenceBackfillAccumulator accumulator,
            UUID sourceDocumentUuid
    ) {
        try {
            accumulator.addSuccess(documentReferenceBackfillItemService.synchronize(sourceDocumentUuid));
        } catch (RuntimeException exception) {
            log.warn(
                    "문서 참조 backfill 처리에 실패했습니다. sourceDocumentUuid={}, causeType={}",
                    sourceDocumentUuid,
                    exception.getClass().getSimpleName()
            );
            accumulator.addFailure(sourceDocumentUuid, exception);
        }
    }

    private long findLastDocumentId(List<CrewDocumentIdentifierReadModel> identifiers) {
        CrewDocumentIdentifierReadModel lastIdentifier = identifiers.get(identifiers.size() - 1);
        return lastIdentifier.id();
    }
}
