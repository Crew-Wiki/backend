package com.wooteco.wiki.document.exception

data class DocumentCreateRequest(
    val title: String,
    val contents: String,
    val writer: String,
    val documentBytes: Long,
)
