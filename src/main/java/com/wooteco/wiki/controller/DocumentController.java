package com.wooteco.wiki.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.wooteco.wiki.dto.DocumentCreateRequest;
import com.wooteco.wiki.dto.DocumentFindAllByRecentResponse;
import com.wooteco.wiki.dto.DocumentResponse;
import com.wooteco.wiki.dto.DocumentUpdateRequest;
import com.wooteco.wiki.dto.ErrorResponse;
import com.wooteco.wiki.dto.LogDetailResponse;
import com.wooteco.wiki.dto.LogResponse;
import com.wooteco.wiki.service.DocumentSearchService;
import com.wooteco.wiki.service.DocumentService;
import com.wooteco.wiki.service.LogService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final LogService logService;
    private final DocumentSearchService documentSearchService;

    @PostMapping("")
    public ResponseEntity<DocumentResponse> post(@RequestBody DocumentCreateRequest documentCreateRequest) {
        DocumentResponse response = documentService.post(documentCreateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<?> getRandom() {
        DocumentResponse response = documentService.getRandom();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> get(@PathVariable String title) {
        Optional<DocumentResponse> response = documentService.get(title);
        if (response.isEmpty()) {
            return notFound();
        }
        return ResponseEntity.ok(response.get());
    }

    private ResponseEntity<ErrorResponse> notFound() {
        return ResponseEntity.status(NOT_FOUND)
                .body(new ErrorResponse("없는 문서입니다."));
    }

    @GetMapping("/{title}/log")
    public ResponseEntity<List<LogResponse>> getLogs(@PathVariable String title) {
        return ResponseEntity.ok(logService.getLogs(title));
    }

    @GetMapping("/log/{logId}")
    public ResponseEntity<LogDetailResponse> getDocumentLogs(@PathVariable Long logId) {
        LogDetailResponse logDetail = logService.getLogDetail(logId);
        return ResponseEntity.ok(logDetail);
    }

    @PutMapping("/{title}")
    public ResponseEntity<DocumentResponse> put(@PathVariable String title,
                                                @RequestBody DocumentUpdateRequest documentUpdateRequest) {
        DocumentResponse response = documentService.put(title, documentUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    public ResponseEntity<DocumentFindAllByRecentResponse> getRecentDocuments() {
        DocumentFindAllByRecentResponse response = documentService.getRecentDocuments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public List<String> search(@RequestParam String keyWord) {
        return documentSearchService.search(keyWord);
    }
}
