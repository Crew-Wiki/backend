package com.wooteco.wiki.service;

import com.wooteco.wiki.dto.DocumentCreateRequest;
import com.wooteco.wiki.dto.DocumentResponse;
import java.util.Optional;

public interface DocumentService {
    DocumentResponse post(String title, DocumentCreateRequest documentCreateRequest);

    Optional<DocumentResponse> get(String title);
}
