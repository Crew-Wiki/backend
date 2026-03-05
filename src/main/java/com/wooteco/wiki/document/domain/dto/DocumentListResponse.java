package com.wooteco.wiki.document.domain.dto;

import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.DocumentType;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentListResponse(
        Long id,
        String title,
        String contents,
        String writer,
        Long documentBytes,
        LocalDateTime generateTime,
        UUID uuid,
        Integer viewCount,
        DocumentType documentType
) {
    public static DocumentListResponse from(Document document) {
        return new DocumentListResponse(
                document.getId(),
                document.getTitle(),
                document.getContents(),
                document.getWriter(),
                document.getDocumentBytes(),
                document.getGenerateTime(),
                document.getUuid(),
                document.getViewCount(),
                document.type()
        );
    }
}
