package com.wooteco.wiki.graph.dto;

import java.util.UUID;

public record AdminDocumentReferenceBackfillFailureResponse(
        UUID sourceDocumentUuid,
        String causeType
) {

    public static AdminDocumentReferenceBackfillFailureResponse from(DocumentReferenceBackfillFailure failure) {
        return new AdminDocumentReferenceBackfillFailureResponse(
                failure.sourceDocumentUuid(),
                failure.causeType()
        );
    }
}
