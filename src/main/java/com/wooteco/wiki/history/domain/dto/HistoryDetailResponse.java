package com.wooteco.wiki.history.domain.dto;

import java.time.LocalDateTime;

public record HistoryDetailResponse(Long historyId, String title, String contents, String writer,
                                    LocalDateTime generateTime) {
}
