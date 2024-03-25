package com.wooteco.wiki.dto;

import com.wooteco.wiki.entity.Document;
import java.util.List;

public record DocumentFindAllByRecentResponse(List<DocumentRecentResponse> documents) {
    public static DocumentFindAllByRecentResponse of(List<Document> documents) {
        List<DocumentRecentResponse> recentDocuments = documents.stream()
                .map(DocumentRecentResponse::from)
                .toList();
        return new DocumentFindAllByRecentResponse(recentDocuments);
    }
}
