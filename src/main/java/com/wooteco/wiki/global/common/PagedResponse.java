package com.wooteco.wiki.global.common;

public record PagedResponse<T> (int page, int totalPage, T data){

    public static <T> PagedResponse<T> of(int page, int totalPage, T data) {
        return new PagedResponse<>(page, totalPage, data);
    }
}
