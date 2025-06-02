package com.wooteco.wiki.document.exception

import com.wooteco.wiki.document.domain.Document

data class DocumentFindAllByRecentResponse(
    val documents: List<DocumentRecentResponse>
) {
    companion object {
        fun of(documents: List<Document?>): DocumentFindAllByRecentResponse {
            val recentDocuments = documents.filterNotNull().map { DocumentRecentResponse.from(it) }
            return DocumentFindAllByRecentResponse(recentDocuments)
        }
    }
}
