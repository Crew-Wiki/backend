package com.wooteco.wiki.service;

import com.wooteco.wiki.entity.Title;
import com.wooteco.wiki.repository.DocumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {
    private final DocumentRepository documentRepository;

    public List<String> search(String keyWord) {
        return documentRepository.findAllByTitleStartingWith(keyWord)
                .stream()
                .map(Title::getTitle)
                .toList();
    }
}
