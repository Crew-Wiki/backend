package com.wooteco.wiki.document.exception

import java.time.LocalDateTime
import java.util.UUID

data class DocumentResponse(
    val documentId: Long,
    var documentUUID: UUID,
    val title: String,
    val contents: String,
    val writer: String,
    val generateTime: LocalDateTime,
)
