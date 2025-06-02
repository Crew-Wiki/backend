package com.wooteco.wiki.document.exception

data class DocumentUpdateRequest(
    val title: String,
    val contents: String,
    val writer: String,
    val documentBytes: Long,
)
