package com.wooteco.wiki.dto;

import com.wooteco.wiki.entity.Document;
import java.time.LocalDateTime;

public record DocumentRecentResponse(Long documentId, String title, LocalDateTime generateTime) {
    public static DocumentRecentResponse from(Document document) {
        return new DocumentRecentResponse(document.getDocumentId(), document.getTitle(), document.getGenerateTime());
    }
}
