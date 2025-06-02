package com.wooteco.wiki.document.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import java.time.LocalDateTime
import java.util.UUID

@Entity
class Document(
    @Column(nullable = false, unique = true)
    var title: String = "",
    @Lob
    var contents: String = "",
    var writer: String = "",
    var documentBytes: Long = 0,
    var generateTime: LocalDateTime = LocalDateTime.now(),
    var uuid: UUID = UUID.randomUUID(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val documentId: Long? = null,
) {

    fun update(
        title: String,
        contents: String,
        writer: String,
        documentBytes: Long,
        generateTime: LocalDateTime
    ) = apply {
        this.title = title
        this.contents = contents
        this.writer = writer
        this.documentBytes = documentBytes
        this.generateTime = generateTime
    }

    override fun equals(other: Any?): Boolean =
        other is Document && this.documentId == other.documentId;

    override fun hashCode(): Int = documentId?.hashCode() ?: 0
}
