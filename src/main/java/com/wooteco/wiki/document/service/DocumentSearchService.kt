package com.wooteco.wiki.document.service

import com.wooteco.wiki.document.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DocumentSearchService(
    private val documentRepository: DocumentRepository,
) {

    fun search(keyWord: String): List<String> =
        documentRepository.findAllByTitleStartingWith(keyWord)
            .map { it.title }
}
