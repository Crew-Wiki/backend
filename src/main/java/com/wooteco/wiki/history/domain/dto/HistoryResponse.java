package com.wooteco.wiki.history.domain.dto;

import com.wooteco.wiki.history.domain.History;
import java.time.LocalDateTime;

public record HistoryResponse(
        Long id,
        String title,
        Long version,
        String writer,
        Long documentBytes,
        LocalDateTime generateTime
) {

    public static HistoryResponse of(History history) {
        return new HistoryResponse(
                history.getId(),
                history.getTitle(),
                history.getVersion(),
                history.getWriter(),
                history.getDocumentBytes(),
                history.getGenerateTime()
        );
    }
}
