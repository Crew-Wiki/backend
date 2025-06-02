package com.wooteco.wiki.log.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class Log(
    var title: String = "",
    var contents: String = "",
    var writer: String = "",
    var documentBytes: Long = 0,
    var generateTime: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val logId: Long? = null
) {

    override fun equals(other: Any?): Boolean =
        other is Log && this.logId == other.logId;

    override fun hashCode(): Int = logId?.hashCode() ?: 0
}
