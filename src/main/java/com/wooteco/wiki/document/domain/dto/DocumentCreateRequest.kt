package com.wooteco.wiki.document.domain.dto

import java.util.UUID

data class DocumentCreateRequest(
    val title: String,
    val contents: String,
    val writer: String,
    val documentBytes: Long,
    val uuid: UUID
)
