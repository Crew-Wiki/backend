package com.wooteco.wiki.document.exception

import com.wooteco.wiki.document.domain.Document
import java.time.LocalDateTime
import java.util.*

data class DocumentRecentResponse(
    val documentId: Long?,
    val uuid: UUID,
    val title: String,
    val generateTime: LocalDateTime,
) {
    companion object {
        fun from(document: Document) = DocumentRecentResponse(
            documentId = document.documentId,
            uuid = document.uuid,
            title = document.title,
            generateTime = document.generateTime,
        )
    }
}
