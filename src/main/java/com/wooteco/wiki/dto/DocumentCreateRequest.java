package com.wooteco.wiki.dto;

import lombok.NonNull;
import org.antlr.v4.runtime.misc.NotNull;

public record DocumentCreateRequest(
        @NotNull String title, @NonNull String contents, @NonNull String writer, @NotNull Long documentBytes) {
}
