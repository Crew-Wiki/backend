package com.wooteco.wiki.graph.dto;

import java.util.List;
import java.util.function.ToIntFunction;

public record DocumentReferenceBackfillResult(
        int processedCount,
        int succeededCount,
        int failedCount,
        int addedCount,
        int removedCount,
        int excludedSelfCount,
        int excludedMissingCount,
        List<DocumentReferenceBackfillFailure> failures
) {

    public DocumentReferenceBackfillResult {
        failures = List.copyOf(failures);
    }

    public static DocumentReferenceBackfillResult of(
            List<DocumentReferenceBackfillItemResult> itemResults,
            List<DocumentReferenceBackfillFailure> failures
    ) {
        return new DocumentReferenceBackfillResult(
                itemResults.size() + failures.size(),
                itemResults.size(),
                failures.size(),
                sum(itemResults, DocumentReferenceBackfillItemResult::addedCount),
                sum(itemResults, DocumentReferenceBackfillItemResult::removedCount),
                sum(itemResults, DocumentReferenceBackfillItemResult::excludedSelfCount),
                sum(itemResults, DocumentReferenceBackfillItemResult::excludedMissingCount),
                failures
        );
    }

    private static int sum(
            List<DocumentReferenceBackfillItemResult> itemResults,
            ToIntFunction<DocumentReferenceBackfillItemResult> countExtractor
    ) {
        return itemResults.stream()
                .mapToInt(countExtractor)
                .sum();
    }
}
