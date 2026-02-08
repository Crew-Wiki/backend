package com.wooteco.wiki.history.service;

import static com.wooteco.wiki.global.exception.ErrorCode.DOCUMENT_NOT_FOUND;
import static com.wooteco.wiki.global.exception.ErrorCode.VERSION_NOT_FOUND;

import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.common.PageRequestDto;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.history.domain.History;
import com.wooteco.wiki.history.domain.dto.HistoryDetailResponse;
import com.wooteco.wiki.history.domain.dto.HistoryResponse;
import com.wooteco.wiki.history.repository.HistoryRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final DocumentRepository documentRepository;

    public void save(Document document) {
        Long maxVersion = historyRepository.findMaxVersionByDocumentId(document.getId())
                .orElse(0L);

        History history = new History(document.getTitle(), document.getContents(), document.getWriter(),
                document.getDocumentBytes(), document.getGenerateTime(), document, maxVersion + 1);
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public HistoryDetailResponse getHistoryDetail(Long historyId) {
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new WikiException(DOCUMENT_NOT_FOUND));
        return new HistoryDetailResponse(historyId, history.getTitle(), history.getContents(), history.getWriter(),
                history.getGenerateTime());
    }

    @Transactional(readOnly = true)
    public Page<HistoryResponse> findAllByDocumentUuid(UUID documentUuid, PageRequestDto pageRequestDto) {
        Long documentId = documentRepository.findIdByUuid(documentUuid)
                .orElseThrow(() -> new WikiException(DOCUMENT_NOT_FOUND));

        Pageable pageable = pageRequestDto.toPageable();
        Page<History> histories = historyRepository.findAllByDocumentId(documentId, pageable);
        List<History> content = histories.getContent();

        List<HistoryResponse> responses = content.stream()
                .map(HistoryResponse::of)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, histories.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Long findLatestVersionByDocument(Document document) {
        return historyRepository.findMaxVersionByDocumentId(document.getId())
                .orElseThrow(() -> new WikiException(VERSION_NOT_FOUND));
    }
}
