package com.wooteco.wiki.document.domain.dto

import com.wooteco.wiki.document.domain.DocumentType
import java.util.*

data class DocumentSearchResponse(
    val title: String,
    val uuid: UUID,
    val documentType: DocumentType
)
