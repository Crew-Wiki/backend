package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillFailure;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillItemResult;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class DocumentReferenceBackfillAccumulator {

    private final List<DocumentReferenceBackfillItemResult> itemResults = new ArrayList<>();
    private final List<DocumentReferenceBackfillFailure> failures = new ArrayList<>();

    void addSuccess(DocumentReferenceBackfillItemResult itemResult) {
        itemResults.add(itemResult);
    }

    void addFailure(
            UUID sourceDocumentUuid,
            Throwable cause
    ) {
        failures.add(DocumentReferenceBackfillFailure.of(sourceDocumentUuid, cause));
    }

    DocumentReferenceBackfillResult toResult() {
        return DocumentReferenceBackfillResult.of(itemResults, failures);
    }
}
