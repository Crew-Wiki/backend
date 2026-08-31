package com.wooteco.wiki.graph.dto;

public record DocumentReferenceBackfillItemResult(
        int addedCount,
        int removedCount,
        int excludedSelfCount,
        int excludedMissingCount,
        boolean mismatched
) {

    public static DocumentReferenceBackfillItemResult of(
            DocumentReferenceSyncResult syncResult,
            boolean mismatched
    ) {
        return new DocumentReferenceBackfillItemResult(
                syncResult.addedCount(),
                syncResult.removedCount(),
                syncResult.excludedSelfCount(),
                syncResult.excludedMissingCount(),
                mismatched
        );
    }
}
