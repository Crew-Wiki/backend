package com.wooteco.wiki.document.domain.dto

data class DocumentUpdateRequest(
    val title: String,
    val contents: String,
    val writer: String,
    val documentBytes: Long,
    val uuid: String
)

