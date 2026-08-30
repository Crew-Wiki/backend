package com.wooteco.wiki.graph.dto;

import java.util.Set;
import java.util.UUID;

public record DocumentReferenceSyncResult(
        Set<UUID> expectedTargetDocumentUuids,
        int extractedCount,
        int addedCount,
        int removedCount,
        int excludedSelfCount,
        int excludedMissingCount
) {

    public DocumentReferenceSyncResult {
        expectedTargetDocumentUuids = Set.copyOf(expectedTargetDocumentUuids);
    }

    public static DocumentReferenceSyncResult of(
            Set<UUID> expectedTargetDocumentUuids,
            int extractedCount,
            int addedCount,
            int removedCount,
            int excludedSelfCount,
            int excludedMissingCount
    ) {
        return new DocumentReferenceSyncResult(
                expectedTargetDocumentUuids,
                extractedCount,
                addedCount,
                removedCount,
                excludedSelfCount,
                excludedMissingCount
        );
    }

    public int validCount() {
        return expectedTargetDocumentUuids.size();
    }
}
