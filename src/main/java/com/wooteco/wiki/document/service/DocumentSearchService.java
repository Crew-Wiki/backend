package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.domain.dto.DocumentSearchResponse;
import com.wooteco.wiki.document.repository.DocumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class DocumentSearchService {

    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<DocumentSearchResponse> search(String keyWord) {
        return documentRepository.findAllByTitleStartingWith(keyWord)
                .stream()
                .map(document -> new DocumentSearchResponse(
                        document.getTitle(),
                        document.getUuid(),
                        document.type()
                ))
                .toList();
    }
}

