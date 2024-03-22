package com.wooteco.wiki.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.wooteco.wiki.dto.DocumentCreateRequest;
import com.wooteco.wiki.dto.DocumentResponse;
import com.wooteco.wiki.dto.ErrorResponse;
import com.wooteco.wiki.service.DocumentService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/{title}")
    public ResponseEntity<DocumentResponse> post(@PathVariable String title,
                                                 @RequestBody DocumentCreateRequest documentCreateRequest) {
        DocumentResponse response = documentService.post(title, documentCreateRequest);
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
}
