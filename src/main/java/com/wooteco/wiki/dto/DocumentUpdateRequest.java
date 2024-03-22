package com.wooteco.wiki.dto;

import lombok.NonNull;

public record DocumentUpdateRequest(@NonNull String contents, @NonNull String writer) {
}
