package com.wooteco.wiki.graph.dto;

public record DocumentReferenceBackfillItemResult(
        int addedCount,
        int removedCount,
        int excludedSelfCount,
        int excludedMissingCount
) {

    public static DocumentReferenceBackfillItemResult of(DocumentReferenceSyncResult syncResult) {
        return new DocumentReferenceBackfillItemResult(
                syncResult.addedCount(),
                syncResult.removedCount(),
                syncResult.excludedSelfCount(),
                syncResult.excludedMissingCount()
        );
    }
}
