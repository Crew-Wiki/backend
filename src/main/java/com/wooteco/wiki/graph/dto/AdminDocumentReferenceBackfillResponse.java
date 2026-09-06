package com.wooteco.wiki.graph.dto;

import java.util.List;

public record AdminDocumentReferenceBackfillResponse(
        int processedCount,
        int succeededCount,
        int failedCount,
        int addedCount,
        int removedCount,
        int excludedSelfCount,
        int excludedMissingCount,
        List<AdminDocumentReferenceBackfillFailureResponse> failures
) {

    public AdminDocumentReferenceBackfillResponse {
        failures = List.copyOf(failures);
    }

    public static AdminDocumentReferenceBackfillResponse from(DocumentReferenceBackfillResult result) {
        return new AdminDocumentReferenceBackfillResponse(
                result.processedCount(),
                result.succeededCount(),
                result.failedCount(),
                result.addedCount(),
                result.removedCount(),
                result.excludedSelfCount(),
                result.excludedMissingCount(),
                createFailures(result.failures())
        );
    }

    private static List<AdminDocumentReferenceBackfillFailureResponse> createFailures(
            List<DocumentReferenceBackfillFailure> failures
    ) {
        return failures.stream()
                .map(AdminDocumentReferenceBackfillFailureResponse::from)
                .toList();
    }
}
