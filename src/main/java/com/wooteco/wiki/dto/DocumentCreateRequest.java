package com.wooteco.wiki.dto;

import lombok.NonNull;

public record DocumentCreateRequest(@NonNull String contents, @NonNull String writer) {
}
