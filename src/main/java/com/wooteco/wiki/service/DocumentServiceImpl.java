package com.wooteco.wiki.service;

import com.wooteco.wiki.dto.DocumentCreateRequest;
import com.wooteco.wiki.dto.DocumentResponse;
import com.wooteco.wiki.dto.DocumentUpdateRequest;
import com.wooteco.wiki.entity.Document;
import com.wooteco.wiki.entity.Log;
import com.wooteco.wiki.repository.DocumentRepository;
import com.wooteco.wiki.repository.LogRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final LogRepository logRepository;

    @Override
    public DocumentResponse post(String title, DocumentCreateRequest documentCreateRequest) {
        String contents = documentCreateRequest.contents();
        String writer = documentCreateRequest.writer();
        Document document = Document.builder()
                .title(title)
                .contents(contents)
                .writer(writer)
                .generateTime(LocalDateTime.now())
                .build();
        try {
            Document save = documentRepository.save(document);
            Log log = new Log(null, title, contents, writer, save.getGenerateTime());
            logRepository.save(log);
            return mapToResponse(save);
        } catch (RuntimeException e) {
            throw new IllegalStateException("제목이 겹치는 문서가 있습니다.");
        }
    }

    @Override
    public Optional<DocumentResponse> get(String title) {
        Optional<Document> byTitle = documentRepository.findByTitle(title);
        return byTitle.map(this::mapToResponse);
    }

    @Override
    public DocumentResponse put(String title, DocumentUpdateRequest documentUpdateRequest) {
        Document document = documentRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다."));
        document.update(documentUpdateRequest.contents(), documentUpdateRequest.writer());

        return mapToResponse(document);
    }

    private DocumentResponse mapToResponse(Document document) {
        long documentId = document.getDocumentId();
        String title = document.getTitle();
        String contents = document.getContents();
        String writer = document.getWriter();
        LocalDateTime generateTime = document.getGenerateTime();
        return new DocumentResponse(documentId, title, contents, writer, generateTime);
    }
}
